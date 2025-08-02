package com.erp.system.bigdata.service;

import com.erp.system.bigdata.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.types.DataTypes;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SparkAnalyticsService {
    
    private final SparkSession sparkSession;
    private final DataLakeService dataLakeService;
    private final MLPipelineService mlPipelineService;
    
    /**
     * Execute complex sales analytics using Spark SQL
     */
    public SalesAnalyticsDto executeSalesAnalytics(LocalDate fromDate, LocalDate toDate) {
        log.info("Executing sales analytics from {} to {}", fromDate, toDate);
        
        try {
            // Load sales data from data lake
            Dataset<Row> salesData = sparkSession
                    .read()
                    .format("delta")
                    .load("s3a://erp-datalake/sales/transactions")
                    .filter(col("transaction_date").between(fromDate.toString(), toDate.toString()));
            
            // Load customer data
            Dataset<Row> customerData = sparkSession
                    .read()
                    .format("parquet")
                    .load("s3a://erp-datalake/customers/customer_master");
            
            // Load product data
            Dataset<Row> productData = sparkSession
                    .read()
                    .format("parquet")
                    .load("s3a://erp-datalake/products/product_master");
            
            // Join datasets
            Dataset<Row> enrichedSales = salesData
                    .join(customerData, "customer_id")
                    .join(productData, "product_id");
            
            // Cache for multiple operations
            enrichedSales.cache();
            
            // Calculate total revenue
            Row totalRevenueRow = enrichedSales
                    .agg(sum("amount").alias("total_revenue"))
                    .first();
            double totalRevenue = totalRevenueRow.getDouble(0);
            
            // Revenue by category
            Dataset<Row> revenueByCategory = enrichedSales
                    .groupBy("product_category")
                    .agg(
                        sum("amount").alias("revenue"),
                        count("*").alias("transaction_count"),
                        avg("amount").alias("avg_transaction_value")
                    )
                    .orderBy(desc("revenue"));
            
            Map<String, Double> categoryRevenue = revenueByCategory.collectAsList().stream()
                    .collect(Collectors.toMap(
                        row -> row.getString(0),
                        row -> row.getDouble(1)
                    ));
            
            // Sales trend analysis
            Dataset<Row> dailyTrends = enrichedSales
                    .groupBy("transaction_date")
                    .agg(
                        sum("amount").alias("daily_revenue"),
                        count("*").alias("daily_transactions")
                    )
                    .orderBy("transaction_date");
            
            // Customer segmentation using RFM analysis
            Dataset<Row> customerRFM = executeRFMAnalysis(enrichedSales);
            
            // Product performance analysis
            Dataset<Row> productPerformance = enrichedSales
                    .groupBy("product_id", "product_name")
                    .agg(
                        sum("amount").alias("product_revenue"),
                        sum("quantity").alias("total_quantity"),
                        countDistinct("customer_id").alias("unique_customers"),
                        avg("amount").alias("avg_order_value")
                    )
                    .orderBy(desc("product_revenue"))
                    .limit(50);
            
            // Seasonal analysis
            Dataset<Row> seasonalTrends = enrichedSales
                    .withColumn("month", month(col("transaction_date")))
                    .withColumn("quarter", quarter(col("transaction_date")))
                    .groupBy("quarter", "month")
                    .agg(
                        sum("amount").alias("revenue"),
                        count("*").alias("transactions")
                    )
                    .orderBy("quarter", "month");
            
            // Geographic analysis
            Dataset<Row> geoAnalysis = enrichedSales
                    .groupBy("customer_region", "customer_country")
                    .agg(
                        sum("amount").alias("region_revenue"),
                        countDistinct("customer_id").alias("unique_customers"),
                        avg("amount").alias("avg_transaction_value")
                    )
                    .orderBy(desc("region_revenue"));
            
            // Advanced analytics: Customer Lifetime Value prediction
            Dataset<Row> clvPredictions = mlPipelineService.predictCustomerLifetimeValue(enrichedSales);
            
            // Churn probability analysis
            Dataset<Row> churnAnalysis = mlPipelineService.analyzeCustomerChurn(enrichedSales);
            
            // Build response
            return SalesAnalyticsDto.builder()
                    .totalRevenue(totalRevenue)
                    .revenueByCategory(categoryRevenue)
                    .dailyTrends(convertToMap(dailyTrends, "transaction_date", "daily_revenue"))
                    .customerSegments(convertRFMToSegments(customerRFM))
                    .topProducts(convertToProductList(productPerformance))
                    .seasonalTrends(convertToSeasonalMap(seasonalTrends))
                    .geographicDistribution(convertToGeoMap(geoAnalysis))
                    .clvPredictions(convertToCLVList(clvPredictions))
                    .churnRisks(convertToChurnList(churnAnalysis))
                    .build();
            
        } catch (Exception e) {
            log.error("Error executing sales analytics", e);
            throw new RuntimeException("Failed to execute sales analytics: " + e.getMessage());
        }
    }
    
    /**
     * Real-time streaming analytics for production monitoring
     */
    public void startProductionStreamAnalytics() {
        log.info("Starting real-time production stream analytics");
        
        try {
            // Read from Kafka stream
            Dataset<Row> productionStream = sparkSession
                    .readStream()
                    .format("kafka")
                    .option("kafka.bootstrap.servers", "localhost:9092")
                    .option("subscribe", "production-events")
                    .load()
                    .select(
                        from_json(col("value").cast("string"), getProductionEventSchema()).alias("data")
                    )
                    .select("data.*");
            
            // Real-time aggregations
            Dataset<Row> productionMetrics = productionStream
                    .withWatermark("timestamp", "10 minutes")
                    .groupBy(
                        window(col("timestamp"), "5 minutes"),
                        col("production_line"),
                        col("product_type")
                    )
                    .agg(
                        sum("quantity_produced").alias("total_quantity"),
                        avg("efficiency_percentage").alias("avg_efficiency"),
                        sum("defect_count").alias("total_defects"),
                        count("*").alias("event_count")
                    );
            
            // Quality alerts
            Dataset<Row> qualityAlerts = productionStream
                    .filter(col("defect_rate").gt(0.05)) // > 5% defect rate
                    .select(
                        col("production_line"),
                        col("product_type"),
                        col("defect_rate"),
                        col("timestamp")
                    );
            
            // Write to console for monitoring (in production, write to alerting system)
            productionMetrics.writeStream()
                    .outputMode("append")
                    .format("console")
                    .option("truncate", false)
                    .start();
            
            // Write quality alerts to Kafka
            qualityAlerts.writeStream()
                    .format("kafka")
                    .option("kafka.bootstrap.servers", "localhost:9092")
                    .option("topic", "quality-alerts")
                    .option("checkpointLocation", "/tmp/spark-checkpoints/quality-alerts")
                    .start();
            
            log.info("Production stream analytics started successfully");
            
        } catch (Exception e) {
            log.error("Error starting production stream analytics", e);
            throw new RuntimeException("Failed to start stream analytics: " + e.getMessage());
        }
    }
    
    /**
     * Execute RFM (Recency, Frequency, Monetary) analysis for customer segmentation
     */
    private Dataset<Row> executeRFMAnalysis(Dataset<Row> salesData) {
        log.debug("Executing RFM analysis");
        
        // Calculate reference date (latest transaction date)
        String maxDate = salesData
                .agg(max("transaction_date"))
                .first()
                .getString(0);
        
        // Calculate RFM metrics per customer
        Dataset<Row> rfmMetrics = salesData
                .groupBy("customer_id")
                .agg(
                    // Recency: days since last purchase
                    datediff(lit(maxDate), max("transaction_date")).alias("recency"),
                    // Frequency: number of transactions
                    count("*").alias("frequency"),
                    // Monetary: total amount spent
                    sum("amount").alias("monetary")
                );
        
        // Calculate RFM scores using quantiles
        Dataset<Row> rfmWithScores = rfmMetrics
                .withColumn("recency_score", 
                    when(col("recency").leq(30), 5)
                    .when(col("recency").leq(60), 4)
                    .when(col("recency").leq(90), 3)
                    .when(col("recency").leq(180), 2)
                    .otherwise(1))
                .withColumn("frequency_score",
                    when(col("frequency").geq(10), 5)
                    .when(col("frequency").geq(7), 4)
                    .when(col("frequency").geq(5), 3)
                    .when(col("frequency").geq(3), 2)
                    .otherwise(1))
                .withColumn("monetary_score",
                    when(col("monetary").geq(10000), 5)
                    .when(col("monetary").geq(5000), 4)
                    .when(col("monetary").geq(2000), 3)
                    .when(col("monetary").geq(1000), 2)
                    .otherwise(1))
                .withColumn("rfm_score", 
                    concat(col("recency_score"), col("frequency_score"), col("monetary_score")));
        
        return rfmWithScores;
    }
    
    /**
     * Execute advanced inventory optimization using machine learning
     */
    public InventoryOptimizationDto executeInventoryOptimization() {
        log.info("Executing advanced inventory optimization");
        
        try {
            // Load historical inventory data
            Dataset<Row> inventoryHistory = sparkSession
                    .read()
                    .format("delta")
                    .load("s3a://erp-datalake/inventory/movements");
            
            // Load sales forecast
            Dataset<Row> salesForecast = sparkSession
                    .read()
                    .format("parquet")
                    .load("s3a://erp-datalake/forecasts/sales_forecast");
            
            // Calculate ABC analysis
            Dataset<Row> abcAnalysis = inventoryHistory
                    .groupBy("product_id")
                    .agg(
                        sum("quantity").alias("total_quantity"),
                        sum("value").alias("total_value"),
                        avg("unit_cost").alias("avg_unit_cost")
                    )
                    .withColumn("value_rank", 
                        rank().over(Window.orderBy(desc("total_value"))))
                    .withColumn("abc_category",
                        when(col("value_rank").leq(functions.lit(0.2).multiply(count("*").over())), "A")
                        .when(col("value_rank").leq(functions.lit(0.5).multiply(count("*").over())), "B")
                        .otherwise("C"));
            
            // Calculate safety stock using statistical methods
            Dataset<Row> safetyStockCalculation = inventoryHistory
                    .groupBy("product_id")
                    .agg(
                        stddev("daily_demand").alias("demand_std"),
                        avg("lead_time").alias("avg_lead_time"),
                        stddev("lead_time").alias("lead_time_std")
                    )
                    .withColumn("safety_stock",
                        lit(1.65).multiply(  // 95% service level
                            sqrt(
                                col("avg_lead_time").multiply(pow(col("demand_std"), 2))
                                .plus(pow(col("lead_time_std"), 2).multiply(pow(col("demand_std"), 2)))
                            )
                        ));
            
            // Economic Order Quantity (EOQ) calculation
            Dataset<Row> eoqCalculation = inventoryHistory
                    .groupBy("product_id")
                    .agg(
                        avg("annual_demand").alias("annual_demand"),
                        avg("ordering_cost").alias("ordering_cost"),
                        avg("holding_cost_rate").alias("holding_cost_rate"),
                        avg("unit_cost").alias("unit_cost")
                    )
                    .withColumn("eoq",
                        sqrt(
                            lit(2).multiply(col("annual_demand")).multiply(col("ordering_cost"))
                            .divide(col("holding_cost_rate").multiply(col("unit_cost")))
                        ));
            
            // Reorder point calculation
            Dataset<Row> reorderPoints = safetyStockCalculation
                    .join(eoqCalculation, "product_id")
                    .join(salesForecast, "product_id")
                    .withColumn("reorder_point",
                        col("avg_daily_demand").multiply(col("avg_lead_time"))
                        .plus(col("safety_stock")));
            
            // Identify slow-moving and obsolete inventory
            Dataset<Row> slowMovingAnalysis = inventoryHistory
                    .filter(col("transaction_date").gt(LocalDate.now().minusDays(90).toString()))
                    .groupBy("product_id")
                    .agg(
                        sum("quantity").alias("recent_movement"),
                        max("transaction_date").alias("last_movement_date")
                    )
                    .withColumn("days_since_movement",
                        datediff(lit(LocalDate.now().toString()), col("last_movement_date")))
                    .withColumn("movement_category",
                        when(col("days_since_movement").gt(180), "Obsolete")
                        .when(col("days_since_movement").gt(90), "Slow Moving")
                        .otherwise("Active"));
            
            return InventoryOptimizationDto.builder()
                    .abcAnalysis(convertToABCMap(abcAnalysis))
                    .safetyStockRecommendations(convertToSafetyStockMap(safetyStockCalculation))
                    .eoqRecommendations(convertToEOQMap(eoqCalculation))
                    .reorderPoints(convertToReorderPointMap(reorderPoints))
                    .slowMovingItems(convertToSlowMovingList(slowMovingAnalysis))
                    .optimizationScore(calculateOptimizationScore(abcAnalysis, slowMovingAnalysis))
                    .build();
            
        } catch (Exception e) {
            log.error("Error executing inventory optimization", e);
            throw new RuntimeException("Failed to execute inventory optimization: " + e.getMessage());
        }
    }
    
    /**
     * Execute financial risk analysis using advanced statistical methods
     */
    public FinancialRiskAnalysisDto executeFinancialRiskAnalysis() {
        log.info("Executing financial risk analysis");
        
        try {
            // Load financial transactions
            Dataset<Row> transactions = sparkSession
                    .read()
                    .format("delta")
                    .load("s3a://erp-datalake/finance/transactions");
            
            // Load customer payment history
            Dataset<Row> paymentHistory = sparkSession
                    .read()
                    .format("parquet")
                    .load("s3a://erp-datalake/finance/payment_history");
            
            // Calculate Value at Risk (VaR) using historical simulation
            Dataset<Row> dailyReturns = transactions
                    .groupBy("transaction_date")
                    .agg(sum("amount").alias("daily_total"))
                    .withColumn("previous_total", lag("daily_total", 1).over(Window.orderBy("transaction_date")))
                    .withColumn("daily_return", 
                        col("daily_total").minus(col("previous_total")).divide(col("previous_total")))
                    .filter(col("daily_return").isNotNull());
            
            // Calculate VaR at 95% confidence level
            double var95 = dailyReturns.stat().approxQuantile("daily_return", new double[]{0.05}, 0.01)[0];
            
            // Credit risk analysis
            Dataset<Row> creditRisk = paymentHistory
                    .groupBy("customer_id")
                    .agg(
                        avg("days_to_payment").alias("avg_payment_days"),
                        stddev("days_to_payment").alias("payment_variance"),
                        sum(when(col("days_to_payment").gt(30), 1).otherwise(0)).alias("late_payments"),
                        count("*").alias("total_payments")
                    )
                    .withColumn("late_payment_rate", 
                        col("late_payments").divide(col("total_payments")))
                    .withColumn("credit_score",
                        when(col("late_payment_rate").lt(0.1).and(col("avg_payment_days").lt(25)), 5)
                        .when(col("late_payment_rate").lt(0.2).and(col("avg_payment_days").lt(35)), 4)
                        .when(col("late_payment_rate").lt(0.3).and(col("avg_payment_days").lt(45)), 3)
                        .when(col("late_payment_rate").lt(0.5), 2)
                        .otherwise(1));
            
            // Liquidity risk analysis
            Dataset<Row> liquidityRisk = transactions
                    .filter(col("transaction_type").isin("RECEIVABLE", "PAYABLE"))
                    .groupBy("transaction_date", "transaction_type")
                    .agg(sum("amount").alias("daily_amount"))
                    .groupBy("transaction_date")
                    .pivot("transaction_type")
                    .sum("daily_amount")
                    .na().fill(0)
                    .withColumn("net_cash_flow", col("RECEIVABLE").minus(col("PAYABLE")))
                    .withColumn("cumulative_cash_flow", 
                        sum("net_cash_flow").over(Window.orderBy("transaction_date")));
            
            return FinancialRiskAnalysisDto.builder()
                    .valueAtRisk95(var95)
                    .creditRiskDistribution(convertToCreditRiskMap(creditRisk))
                    .liquidityProjection(convertToLiquidityMap(liquidityRisk))
                    .riskScore(calculateOverallRiskScore(var95, creditRisk, liquidityRisk))
                    .recommendations(generateRiskRecommendations(var95, creditRisk))
                    .build();
            
        } catch (Exception e) {
            log.error("Error executing financial risk analysis", e);
            throw new RuntimeException("Failed to execute financial risk analysis: " + e.getMessage());
        }
    }
    
    // Helper methods for data conversion
    private Map<String, Double> convertToMap(Dataset<Row> dataset, String keyCol, String valueCol) {
        return dataset.collectAsList().stream()
                .collect(Collectors.toMap(
                    row -> row.getString(row.fieldIndex(keyCol)),
                    row -> row.getDouble(row.fieldIndex(valueCol))
                ));
    }
    
    private org.apache.spark.sql.types.StructType getProductionEventSchema() {
        return DataTypes.createStructType(new org.apache.spark.sql.types.StructField[]{
            DataTypes.createStructField("timestamp", DataTypes.TimestampType, false),
            DataTypes.createStructField("production_line", DataTypes.StringType, false),
            DataTypes.createStructField("product_type", DataTypes.StringType, false),
            DataTypes.createStructField("quantity_produced", DataTypes.IntegerType, false),
            DataTypes.createStructField("efficiency_percentage", DataTypes.DoubleType, false),
            DataTypes.createStructField("defect_count", DataTypes.IntegerType, false),
            DataTypes.createStructField("defect_rate", DataTypes.DoubleType, false)
        });
    }
    
    // Additional helper methods would be implemented here...
    private Map<String, Object> convertRFMToSegments(Dataset<Row> rfmData) { return new HashMap<>(); }
    private List<Object> convertToProductList(Dataset<Row> productData) { return new ArrayList<>(); }
    private Map<String, Object> convertToSeasonalMap(Dataset<Row> seasonalData) { return new HashMap<>(); }
    private Map<String, Object> convertToGeoMap(Dataset<Row> geoData) { return new HashMap<>(); }
    private List<Object> convertToCLVList(Dataset<Row> clvData) { return new ArrayList<>(); }
    private List<Object> convertToChurnList(Dataset<Row> churnData) { return new ArrayList<>(); }
    private Map<String, Object> convertToABCMap(Dataset<Row> abcData) { return new HashMap<>(); }
    private Map<String, Object> convertToSafetyStockMap(Dataset<Row> safetyStockData) { return new HashMap<>(); }
    private Map<String, Object> convertToEOQMap(Dataset<Row> eoqData) { return new HashMap<>(); }
    private Map<String, Object> convertToReorderPointMap(Dataset<Row> reorderData) { return new HashMap<>(); }
    private List<Object> convertToSlowMovingList(Dataset<Row> slowMovingData) { return new ArrayList<>(); }
    private double calculateOptimizationScore(Dataset<Row> abcData, Dataset<Row> slowMovingData) { return 0.0; }
    private Map<String, Object> convertToCreditRiskMap(Dataset<Row> creditRiskData) { return new HashMap<>(); }
    private Map<String, Object> convertToLiquidityMap(Dataset<Row> liquidityData) { return new HashMap<>(); }
    private double calculateOverallRiskScore(double var95, Dataset<Row> creditRisk, Dataset<Row> liquidityRisk) { return 0.0; }
    private List<String> generateRiskRecommendations(double var95, Dataset<Row> creditRisk) { return new ArrayList<>(); }
}