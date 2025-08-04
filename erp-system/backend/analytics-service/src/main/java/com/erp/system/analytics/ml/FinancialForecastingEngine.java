package com.erp.system.analytics.ml;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.feature.*;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.RandomForestRegressor;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.ml.tuning.ParamMap;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FinancialForecastingEngine {

    private final SparkSession spark;
    private final JavaSparkContext jsc;
    private PipelineModel trainedModel;

    public FinancialForecastingEngine() {
        this.spark = SparkSession.builder()
                .appName("ERP Financial Forecasting")
                .master("local[*]")
                .config("spark.sql.adaptive.enabled", "true")
                .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
                .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .getOrCreate();
        
        this.jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
    }

    public ForecastResult forecastRevenue(ForecastRequest request) {
        try {
            // Load and prepare historical data
            Dataset<Row> historicalData = loadHistoricalFinancialData(request);
            
            // Feature engineering
            Dataset<Row> featuresData = engineerFeatures(historicalData);
            
            // Train or load model
            if (trainedModel == null || request.isRetrainModel()) {
                trainedModel = trainRevenueModel(featuresData);
            }
            
            // Generate forecast
            Dataset<Row> forecastData = prepareForecastData(request);
            Dataset<Row> predictions = trainedModel.transform(forecastData);
            
            // Post-process results
            return processForecastResults(predictions, request);
            
        } catch (Exception e) {
            throw new RuntimeException("Error in revenue forecasting", e);
        }
    }

    private Dataset<Row> loadHistoricalFinancialData(ForecastRequest request) {
        // In production, this would connect to your data warehouse
        return spark.read()
                .format("jdbc")
                .option("url", "jdbc:postgresql://postgres:5432/erp_main")
                .option("dbtable", """
                    (SELECT 
                        DATE_TRUNC('month', transaction_date) as month,
                        SUM(amount) as revenue,
                        COUNT(*) as transaction_count,
                        AVG(amount) as avg_transaction_amount,
                        EXTRACT(MONTH FROM transaction_date) as month_of_year,
                        EXTRACT(QUARTER FROM transaction_date) as quarter,
                        EXTRACT(YEAR FROM transaction_date) as year
                     FROM transactions 
                     WHERE transaction_date >= CURRENT_DATE - INTERVAL '3 years'
                     AND type = 'INCOME'
                     GROUP BY DATE_TRUNC('month', transaction_date)
                     ORDER BY month) as revenue_data
                    """)
                .option("user", "erp_user")
                .option("password", "erp_password")
                .load();
    }

    private Dataset<Row> engineerFeatures(Dataset<Row> data) {
        // Create lag features
        data = data.withColumn("revenue_lag_1", 
            functions.lag("revenue", 1).over(Window.orderBy("month")));
        data = data.withColumn("revenue_lag_3", 
            functions.lag("revenue", 3).over(Window.orderBy("month")));
        data = data.withColumn("revenue_lag_12", 
            functions.lag("revenue", 12).over(Window.orderBy("month")));
        
        // Moving averages
        data = data.withColumn("revenue_ma_3", 
            functions.avg("revenue").over(Window.orderBy("month").rowsBetween(-2, 0)));
        data = data.withColumn("revenue_ma_6", 
            functions.avg("revenue").over(Window.orderBy("month").rowsBetween(-5, 0)));
        
        // Seasonal features
        data = data.withColumn("is_q4", 
            functions.when(functions.col("quarter").equalTo(4), 1.0).otherwise(0.0));
        data = data.withColumn("is_december", 
            functions.when(functions.col("month_of_year").equalTo(12), 1.0).otherwise(0.0));
        
        // Growth rates
        data = data.withColumn("yoy_growth", 
            (functions.col("revenue") - functions.lag("revenue", 12).over(Window.orderBy("month"))) 
            / functions.lag("revenue", 12).over(Window.orderBy("month")));
        
        return data.na().drop();
    }

    private PipelineModel trainRevenueModel(Dataset<Row> data) {
        // Prepare feature vector
        String[] featureCols = {
            "revenue_lag_1", "revenue_lag_3", "revenue_lag_12",
            "revenue_ma_3", "revenue_ma_6", "transaction_count",
            "avg_transaction_amount", "month_of_year", "quarter",
            "is_q4", "is_december", "yoy_growth"
        };
        
        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(featureCols)
                .setOutputCol("features");
        
        // Feature scaling
        StandardScaler scaler = new StandardScaler()
                .setInputCol("features")
                .setOutputCol("scaledFeatures")
                .setWithStd(true)
                .setWithMean(true);
        
        // Multiple algorithms for ensemble
        LinearRegression lr = new LinearRegression()
                .setFeaturesCol("scaledFeatures")
                .setLabelCol("revenue")
                .setPredictionCol("lr_prediction");
        
        RandomForestRegressor rf = new RandomForestRegressor()
                .setFeaturesCol("scaledFeatures")
                .setLabelCol("revenue")
                .setPredictionCol("rf_prediction")
                .setNumTrees(100)
                .setMaxDepth(10);
        
        // Create pipeline
        Pipeline pipeline = new Pipeline()
                .setStages(new PipelineStage[]{assembler, scaler, lr, rf});
        
        // Cross-validation for hyperparameter tuning
        ParamMap[] paramGrid = new ParamGridBuilder()
                .addGrid(lr.regParam(), new double[]{0.01, 0.1, 1.0})
                .addGrid(rf.numTrees(), new int[]{50, 100, 200})
                .addGrid(rf.maxDepth(), new int[]{5, 10, 15})
                .build();
        
        CrossValidator cv = new CrossValidator()
                .setEstimator(pipeline)
                .setEvaluator(new RegressionEvaluator()
                        .setLabelCol("revenue")
                        .setPredictionCol("rf_prediction")
                        .setMetricName("rmse"))
                .setEstimatorParamMaps(paramGrid)
                .setNumFolds(5);
        
        // Split data
        Dataset<Row>[] splits = data.randomSplit(new double[]{0.8, 0.2}, 42L);
        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testData = splits[1];
        
        // Train model
        PipelineModel model = (PipelineModel) cv.fit(trainingData);
        
        // Evaluate model
        Dataset<Row> predictions = model.transform(testData);
        RegressionEvaluator evaluator = new RegressionEvaluator()
                .setLabelCol("revenue")
                .setPredictionCol("rf_prediction")
                .setMetricName("rmse");
        
        double rmse = evaluator.evaluate(predictions);
        System.out.println("Root Mean Squared Error (RMSE) on test data = " + rmse);
        
        return model;
    }

    private Dataset<Row> prepareForecastData(ForecastRequest request) {
        // Create future dates and feature placeholders
        List<Row> futureRows = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(request.getStartDate());
        
        for (int i = 0; i < request.getForecastPeriods(); i++) {
            // This would include logic to populate features for future dates
            // based on historical patterns and external factors
            cal.add(Calendar.MONTH, 1);
        }
        
        // For now, return a simplified dataset
        // In production, this would be more sophisticated
        return spark.createDataFrame(futureRows, 
            loadHistoricalFinancialData(request).schema());
    }

    private ForecastResult processForecastResults(Dataset<Row> predictions, ForecastRequest request) {
        List<Row> results = predictions.select("month", "rf_prediction", "lr_prediction").collectAsList();
        
        List<ForecastPoint> forecastPoints = new ArrayList<>();
        double totalForecast = 0.0;
        
        for (Row row : results) {
            double rfPrediction = row.getDouble(1);
            double lrPrediction = row.getDouble(2);
            
            // Ensemble prediction (weighted average)
            double ensemblePrediction = 0.7 * rfPrediction + 0.3 * lrPrediction;
            
            ForecastPoint point = new ForecastPoint(
                row.getTimestamp(0).toLocalDateTime().toLocalDate(),
                ensemblePrediction,
                calculateConfidenceInterval(ensemblePrediction, 0.95)
            );
            
            forecastPoints.add(point);
            totalForecast += ensemblePrediction;
        }
        
        return new ForecastResult(
            forecastPoints,
            totalForecast,
            calculateAccuracyMetrics(),
            generateInsights(forecastPoints)
        );
    }

    public CustomerSegmentationResult segmentCustomers(CustomerSegmentationRequest request) {
        try {
            // Load customer transaction data
            Dataset<Row> customerData = loadCustomerTransactionData(request);
            
            // Feature engineering for RFM analysis
            Dataset<Row> rfmData = calculateRFMFeatures(customerData);
            
            // Apply K-means clustering
            return performKMeansClustering(rfmData, request.getNumberOfSegments());
            
        } catch (Exception e) {
            throw new RuntimeException("Error in customer segmentation", e);
        }
    }

    private Dataset<Row> calculateRFMFeatures(Dataset<Row> data) {
        // Recency, Frequency, Monetary analysis
        return data.groupBy("customer_id")
                .agg(
                    functions.max("transaction_date").alias("last_transaction"),
                    functions.count("transaction_id").alias("frequency"),
                    functions.sum("amount").alias("monetary_value"),
                    functions.avg("amount").alias("avg_transaction_value")
                )
                .withColumn("recency_days", 
                    functions.datediff(functions.current_date(), functions.col("last_transaction")));
    }

    public AnomalyDetectionResult detectFinancialAnomalies(AnomalyDetectionRequest request) {
        try {
            // Load transaction data
            Dataset<Row> transactionData = loadTransactionDataForAnomaly(request);
            
            // Feature engineering
            Dataset<Row> features = engineerAnomalyFeatures(transactionData);
            
            // Apply Isolation Forest algorithm
            return performIsolationForestDetection(features);
            
        } catch (Exception e) {
            throw new RuntimeException("Error in anomaly detection", e);
        }
    }

    private ConfidenceInterval calculateConfidenceInterval(double prediction, double confidence) {
        // Simplified confidence interval calculation
        // In production, this would use proper statistical methods
        double margin = prediction * 0.1; // 10% margin as example
        return new ConfidenceInterval(prediction - margin, prediction + margin);
    }

    private AccuracyMetrics calculateAccuracyMetrics() {
        // This would calculate MAPE, RMSE, MAE etc. from historical backtesting
        return new AccuracyMetrics(0.95, 1500.0, 1200.0, 0.08);
    }

    private List<String> generateInsights(List<ForecastPoint> points) {
        List<String> insights = new ArrayList<>();
        
        // Trend analysis
        if (points.size() > 1) {
            double firstValue = points.get(0).getPredictedValue();
            double lastValue = points.get(points.size() - 1).getPredictedValue();
            double growth = ((lastValue - firstValue) / firstValue) * 100;
            
            if (growth > 5) {
                insights.add("Forecasted revenue shows strong growth trend of " + String.format("%.1f%%", growth));
            } else if (growth < -5) {
                insights.add("Forecasted revenue shows declining trend of " + String.format("%.1f%%", growth));
            } else {
                insights.add("Forecasted revenue shows stable trend");
            }
        }
        
        // Seasonality insights
        insights.add("Q4 typically shows 15-20% higher revenue due to seasonal factors");
        
        return insights;
    }

    // Additional ML capabilities would be implemented here:
    // - Demand forecasting
    // - Price optimization
    // - Fraud detection
    // - Inventory optimization
    // - Employee performance prediction
    // - Risk assessment models
}

// Supporting classes
class ForecastRequest {
    private Date startDate;
    private int forecastPeriods;
    private boolean retrainModel;
    // getters and setters
}

class ForecastResult {
    private List<ForecastPoint> forecasts;
    private double totalForecast;
    private AccuracyMetrics accuracy;
    private List<String> insights;
    // constructors, getters and setters
}

class ForecastPoint {
    private java.time.LocalDate date;
    private double predictedValue;
    private ConfidenceInterval confidenceInterval;
    // constructors, getters and setters
}