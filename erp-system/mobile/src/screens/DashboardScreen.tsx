import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  ScrollView,
  RefreshControl,
  StyleSheet,
  Dimensions,
  Alert,
  TouchableOpacity,
  StatusBar
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useNavigation } from '@react-navigation/native';
import { useFocusEffect } from '@react-navigation/native';
import { LineChart, BarChart, PieChart } from 'react-native-chart-kit';
import { Card, Title, Paragraph, Button, FAB } from 'react-native-paper';
import { Ionicons } from '@expo/vector-icons';
import { useQuery } from '@apollo/client';
import { useStore } from '../store/useStore';
import { GET_DASHBOARD_DATA } from '../graphql/queries';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorBoundary from '../components/ErrorBoundary';
import NotificationBadge from '../components/NotificationBadge';
import QuickActionButton from '../components/QuickActionButton';

const screenWidth = Dimensions.get('window').width;

interface DashboardData {
  kpis: {
    totalRevenue: number;
    totalOrders: number;
    activeEmployees: number;
    inventoryValue: number;
  };
  revenueChart: {
    labels: string[];
    datasets: Array<{
      data: number[];
    }>;
  };
  salesByCategory: Array<{
    name: string;
    population: number;
    color: string;
    legendFontColor: string;
    legendFontSize: number;
  }>;
  recentActivities: Array<{
    id: string;
    type: string;
    description: string;
    timestamp: string;
    priority: 'HIGH' | 'MEDIUM' | 'LOW';
  }>;
  pendingApprovals: number;
  criticalAlerts: number;
}

const DashboardScreen: React.FC = () => {
  const navigation = useNavigation();
  const { user, theme, setNotificationCount } = useStore();
  const [refreshing, setRefreshing] = useState(false);
  const [selectedPeriod, setSelectedPeriod] = useState('7d');

  const { data, loading, error, refetch } = useQuery<{ dashboard: DashboardData }>(
    GET_DASHBOARD_DATA,
    {
      variables: { period: selectedPeriod },
      errorPolicy: 'partial',
      notifyOnNetworkStatusChange: true,
      pollInterval: 30000, // Refresh every 30 seconds
    }
  );

  useFocusEffect(
    useCallback(() => {
      // Update notification count when screen is focused
      if (data?.dashboard) {
        setNotificationCount(
          data.dashboard.pendingApprovals + data.dashboard.criticalAlerts
        );
      }
    }, [data, setNotificationCount])
  );

  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    try {
      await refetch();
    } catch (err) {
      Alert.alert('Error', 'Failed to refresh dashboard data');
    } finally {
      setRefreshing(false);
    }
  }, [refetch]);

  const handleQuickAction = (action: string) => {
    switch (action) {
      case 'scan_barcode':
        navigation.navigate('BarcodeScanner');
        break;
      case 'create_order':
        navigation.navigate('CreateOrder');
        break;
      case 'check_inventory':
        navigation.navigate('InventorySearch');
        break;
      case 'approve_requests':
        navigation.navigate('Approvals');
        break;
      default:
        break;
    }
  };

  const renderKPICard = (title: string, value: string | number, icon: string, color: string) => (
    <Card style={[styles.kpiCard, { borderLeftColor: color }]}>
      <Card.Content style={styles.kpiContent}>
        <View style={styles.kpiHeader}>
          <Ionicons name={icon as any} size={24} color={color} />
          <Text style={styles.kpiTitle}>{title}</Text>
        </View>
        <Text style={[styles.kpiValue, { color }]}>{value}</Text>
      </Card.Content>
    </Card>
  );

  const renderRecentActivity = (activity: DashboardData['recentActivities'][0]) => (
    <TouchableOpacity
      key={activity.id}
      style={styles.activityItem}
      onPress={() => navigation.navigate('ActivityDetail', { id: activity.id })}
    >
      <View style={styles.activityHeader}>
        <View style={[
          styles.priorityIndicator,
          { backgroundColor: getPriorityColor(activity.priority) }
        ]} />
        <Text style={styles.activityType}>{activity.type}</Text>
        <Text style={styles.activityTime}>
          {new Date(activity.timestamp).toLocaleTimeString()}
        </Text>
      </View>
      <Text style={styles.activityDescription}>{activity.description}</Text>
    </TouchableOpacity>
  );

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH': return '#f44336';
      case 'MEDIUM': return '#ff9800';
      case 'LOW': return '#4caf50';
      default: return '#9e9e9e';
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  if (loading && !data) {
    return <LoadingSpinner />;
  }

  if (error && !data) {
    return (
      <View style={styles.errorContainer}>
        <Ionicons name="alert-circle" size={64} color="#f44336" />
        <Text style={styles.errorText}>Failed to load dashboard</Text>
        <Button mode="contained" onPress={() => refetch()}>
          Retry
        </Button>
      </View>
    );
  }

  const dashboardData = data?.dashboard;

  return (
    <ErrorBoundary>
      <SafeAreaView style={styles.container}>
        <StatusBar barStyle={theme === 'dark' ? 'light-content' : 'dark-content'} />
        
        {/* Header */}
        <View style={styles.header}>
          <View>
            <Text style={styles.greeting}>Good morning,</Text>
            <Text style={styles.userName}>{user?.name || 'User'}</Text>
          </View>
          <View style={styles.headerActions}>
            <NotificationBadge
              count={dashboardData?.pendingApprovals || 0}
              onPress={() => navigation.navigate('Notifications')}
            />
            <TouchableOpacity
              style={styles.profileButton}
              onPress={() => navigation.navigate('Profile')}
            >
              <Ionicons name="person-circle" size={32} color="#2196f3" />
            </TouchableOpacity>
          </View>
        </View>

        <ScrollView
          style={styles.content}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
          }
          showsVerticalScrollIndicator={false}
        >
          {/* Period Selector */}
          <View style={styles.periodSelector}>
            {['1d', '7d', '30d', '90d'].map((period) => (
              <TouchableOpacity
                key={period}
                style={[
                  styles.periodButton,
                  selectedPeriod === period && styles.periodButtonActive
                ]}
                onPress={() => setSelectedPeriod(period)}
              >
                <Text style={[
                  styles.periodButtonText,
                  selectedPeriod === period && styles.periodButtonTextActive
                ]}>
                  {period.toUpperCase()}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          {/* KPI Cards */}
          <View style={styles.kpiContainer}>
            {renderKPICard(
              'Revenue',
              formatCurrency(dashboardData?.kpis.totalRevenue || 0),
              'trending-up',
              '#4caf50'
            )}
            {renderKPICard(
              'Orders',
              dashboardData?.kpis.totalOrders || 0,
              'receipt',
              '#2196f3'
            )}
            {renderKPICard(
              'Employees',
              dashboardData?.kpis.activeEmployees || 0,
              'people',
              '#ff9800'
            )}
            {renderKPICard(
              'Inventory',
              formatCurrency(dashboardData?.kpis.inventoryValue || 0),
              'cube',
              '#9c27b0'
            )}
          </View>

          {/* Quick Actions */}
          <Card style={styles.quickActionsCard}>
            <Card.Content>
              <Title>Quick Actions</Title>
              <View style={styles.quickActionsContainer}>
                <QuickActionButton
                  icon="barcode-scan"
                  label="Scan Barcode"
                  onPress={() => handleQuickAction('scan_barcode')}
                />
                <QuickActionButton
                  icon="add-circle"
                  label="Create Order"
                  onPress={() => handleQuickAction('create_order')}
                />
                <QuickActionButton
                  icon="search"
                  label="Check Inventory"
                  onPress={() => handleQuickAction('check_inventory')}
                />
                <QuickActionButton
                  icon="checkmark-circle"
                  label="Approvals"
                  onPress={() => handleQuickAction('approve_requests')}
                  badge={dashboardData?.pendingApprovals}
                />
              </View>
            </Card.Content>
          </Card>

          {/* Revenue Chart */}
          {dashboardData?.revenueChart && (
            <Card style={styles.chartCard}>
              <Card.Content>
                <Title>Revenue Trend</Title>
                <LineChart
                  data={dashboardData.revenueChart}
                  width={screenWidth - 60}
                  height={220}
                  chartConfig={{
                    backgroundColor: '#ffffff',
                    backgroundGradientFrom: '#ffffff',
                    backgroundGradientTo: '#ffffff',
                    decimalPlaces: 0,
                    color: (opacity = 1) => `rgba(33, 150, 243, ${opacity})`,
                    labelColor: (opacity = 1) => `rgba(0, 0, 0, ${opacity})`,
                    style: {
                      borderRadius: 16,
                    },
                    propsForDots: {
                      r: '6',
                      strokeWidth: '2',
                      stroke: '#2196f3',
                    },
                  }}
                  bezier
                  style={styles.chart}
                />
              </Card.Content>
            </Card>
          )}

          {/* Sales by Category */}
          {dashboardData?.salesByCategory && (
            <Card style={styles.chartCard}>
              <Card.Content>
                <Title>Sales by Category</Title>
                <PieChart
                  data={dashboardData.salesByCategory}
                  width={screenWidth - 60}
                  height={220}
                  chartConfig={{
                    color: (opacity = 1) => `rgba(0, 0, 0, ${opacity})`,
                  }}
                  accessor="population"
                  backgroundColor="transparent"
                  paddingLeft="15"
                  absolute
                />
              </Card.Content>
            </Card>
          )}

          {/* Recent Activities */}
          <Card style={styles.activitiesCard}>
            <Card.Content>
              <View style={styles.activitiesHeader}>
                <Title>Recent Activities</Title>
                <Button
                  mode="text"
                  onPress={() => navigation.navigate('Activities')}
                >
                  View All
                </Button>
              </View>
              {dashboardData?.recentActivities?.map(renderRecentActivity)}
            </Card.Content>
          </Card>

          {/* Bottom spacing for FAB */}
          <View style={{ height: 100 }} />
        </ScrollView>

        {/* Floating Action Button */}
        <FAB
          style={styles.fab}
          icon="plus"
          onPress={() => navigation.navigate('QuickActions')}
        />
      </SafeAreaView>
    </ErrorBoundary>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: '#ffffff',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  greeting: {
    fontSize: 16,
    color: '#666',
  },
  userName: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
  },
  headerActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  profileButton: {
    borderRadius: 20,
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
  },
  periodSelector: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 4,
    marginVertical: 16,
    elevation: 1,
  },
  periodButton: {
    flex: 1,
    paddingVertical: 8,
    alignItems: 'center',
    borderRadius: 8,
  },
  periodButtonActive: {
    backgroundColor: '#2196f3',
  },
  periodButtonText: {
    fontSize: 14,
    color: '#666',
    fontWeight: '500',
  },
  periodButtonTextActive: {
    color: '#ffffff',
  },
  kpiContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginBottom: 16,
  },
  kpiCard: {
    width: '48%',
    marginBottom: 12,
    borderLeftWidth: 4,
    elevation: 2,
  },
  kpiContent: {
    paddingVertical: 12,
  },
  kpiHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  kpiTitle: {
    fontSize: 14,
    color: '#666',
    marginLeft: 8,
  },
  kpiValue: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  quickActionsCard: {
    marginBottom: 16,
    elevation: 2,
  },
  quickActionsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginTop: 12,
  },
  chartCard: {
    marginBottom: 16,
    elevation: 2,
  },
  chart: {
    marginVertical: 8,
    borderRadius: 16,
  },
  activitiesCard: {
    marginBottom: 16,
    elevation: 2,
  },
  activitiesHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  activityItem: {
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  activityHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  priorityIndicator: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 8,
  },
  activityType: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    flex: 1,
  },
  activityTime: {
    fontSize: 12,
    color: '#666',
  },
  activityDescription: {
    fontSize: 14,
    color: '#666',
    marginLeft: 16,
  },
  fab: {
    position: 'absolute',
    margin: 16,
    right: 0,
    bottom: 0,
    backgroundColor: '#2196f3',
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  errorText: {
    fontSize: 18,
    color: '#666',
    marginVertical: 16,
    textAlign: 'center',
  },
});

export default DashboardScreen;