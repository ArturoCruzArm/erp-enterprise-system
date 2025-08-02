'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  UsersIcon,
  BuildingOfficeIcon,
  BriefcaseIcon,
  CalendarDaysIcon,
  ChartBarIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  AdjustmentsHorizontalIcon,
} from '@heroicons/react/24/outline';
import Link from 'next/link';

interface Employee {
  id: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  departmentName: string;
  positionTitle: string;
  status: string;
  hireDate: string;
}

interface HRStats {
  totalEmployees: number;
  activeEmployees: number;
  newHiresThisMonth: number;
  pendingLeaveRequests: number;
  totalDepartments: number;
  avgSalary: number;
}

// Mock data - in real app this would come from API
const mockEmployees: Employee[] = [
  {
    id: 1,
    employeeCode: 'EMP001',
    firstName: 'John',
    lastName: 'Doe',
    email: 'john.doe@company.com',
    departmentName: 'Information Technology',
    positionTitle: 'Senior Software Developer',
    status: 'ACTIVE',
    hireDate: '2023-01-15'
  },
  {
    id: 2,
    employeeCode: 'EMP002',
    firstName: 'Jane',
    lastName: 'Smith',
    email: 'jane.smith@company.com',
    departmentName: 'Information Technology',
    positionTitle: 'IT Manager',
    status: 'ACTIVE',
    hireDate: '2023-02-01'
  },
  {
    id: 3,
    employeeCode: 'EMP003',
    firstName: 'Mike',
    lastName: 'Johnson',
    email: 'mike.johnson@company.com',
    departmentName: 'Human Resources',
    positionTitle: 'HR Specialist',
    status: 'ACTIVE',
    hireDate: '2023-01-20'
  }
];

const mockStats: HRStats = {
  totalEmployees: 156,
  activeEmployees: 148,
  newHiresThisMonth: 12,
  pendingLeaveRequests: 8,
  totalDepartments: 6,
  avgSalary: 75000
};

export default function HRPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('all');

  // Mock query - replace with actual API call
  const { data: employees = mockEmployees, isLoading: employeesLoading } = useQuery({
    queryKey: ['employees'],
    queryFn: async () => {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      return mockEmployees;
    }
  });

  const { data: stats = mockStats, isLoading: statsLoading } = useQuery({
    queryKey: ['hr-stats'],
    queryFn: async () => {
      await new Promise(resolve => setTimeout(resolve, 800));
      return mockStats;
    }
  });

  const filteredEmployees = employees.filter(employee => {
    const matchesSearch = `${employee.firstName} ${employee.lastName} ${employee.email} ${employee.employeeCode}`
      .toLowerCase()
      .includes(searchTerm.toLowerCase());
    const matchesStatus = selectedStatus === 'all' || employee.status === selectedStatus;
    return matchesSearch && matchesStatus;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'INACTIVE':
        return 'bg-gray-100 text-gray-800';
      case 'ON_LEAVE':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Link href="/" className="text-gray-500 hover:text-gray-700">
                ← Back to Dashboard
              </Link>
              <div className="ml-6 flex items-center">
                <UsersIcon className="h-8 w-8 text-blue-600" />
                <h1 className="ml-3 text-2xl font-bold text-gray-900">Human Resources</h1>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <button className="btn-outline">
                <ChartBarIcon className="h-4 w-4 mr-2" />
                Reports
              </button>
              <button className="btn-primary">
                <PlusIcon className="h-4 w-4 mr-2" />
                Add Employee
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="card p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 rounded-lg">
                <UsersIcon className="h-8 w-8 text-blue-600" />
              </div>
              <div className="ml-4">
                <p className="text-2xl font-bold text-gray-900">
                  {statsLoading ? '...' : stats.totalEmployees}
                </p>
                <p className="text-gray-600">Total Employees</p>
              </div>
            </div>
          </div>

          <div className="card p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 rounded-lg">
                <UsersIcon className="h-8 w-8 text-green-600" />
              </div>
              <div className="ml-4">
                <p className="text-2xl font-bold text-gray-900">
                  {statsLoading ? '...' : stats.activeEmployees}
                </p>
                <p className="text-gray-600">Active Employees</p>
              </div>
            </div>
          </div>

          <div className="card p-6">
            <div className="flex items-center">
              <div className="p-2 bg-purple-100 rounded-lg">
                <CalendarDaysIcon className="h-8 w-8 text-purple-600" />
              </div>
              <div className="ml-4">
                <p className="text-2xl font-bold text-gray-900">
                  {statsLoading ? '...' : stats.newHiresThisMonth}
                </p>
                <p className="text-gray-600">New Hires (This Month)</p>
              </div>
            </div>
          </div>

          <div className="card p-6">
            <div className="flex items-center">
              <div className="p-2 bg-orange-100 rounded-lg">
                <BriefcaseIcon className="h-8 w-8 text-orange-600" />
              </div>
              <div className="ml-4">
                <p className="text-2xl font-bold text-gray-900">
                  {statsLoading ? '...' : stats.pendingLeaveRequests}
                </p>
                <p className="text-gray-600">Pending Leave Requests</p>
              </div>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
          <Link href="/hr/employees" className="card p-6 hover:shadow-md transition-shadow">
            <div className="text-center">
              <UsersIcon className="h-12 w-12 text-blue-600 mx-auto mb-4" />
              <h3 className="font-semibold text-gray-900">Manage Employees</h3>
              <p className="text-sm text-gray-600 mt-2">View and manage employee records</p>
            </div>
          </Link>

          <Link href="/hr/departments" className="card p-6 hover:shadow-md transition-shadow">
            <div className="text-center">
              <BuildingOfficeIcon className="h-12 w-12 text-green-600 mx-auto mb-4" />
              <h3 className="font-semibold text-gray-900">Departments</h3>
              <p className="text-sm text-gray-600 mt-2">Organize departments and teams</p>
            </div>
          </Link>

          <Link href="/hr/leave-requests" className="card p-6 hover:shadow-md transition-shadow">
            <div className="text-center">
              <CalendarDaysIcon className="h-12 w-12 text-purple-600 mx-auto mb-4" />
              <h3 className="font-semibold text-gray-900">Leave Management</h3>
              <p className="text-sm text-gray-600 mt-2">Handle leave requests and approvals</p>
            </div>
          </Link>

          <Link href="/hr/performance" className="card p-6 hover:shadow-md transition-shadow">
            <div className="text-center">
              <ChartBarIcon className="h-12 w-12 text-orange-600 mx-auto mb-4" />
              <h3 className="font-semibold text-gray-900">Performance</h3>
              <p className="text-sm text-gray-600 mt-2">Track employee performance</p>
            </div>
          </Link>
        </div>

        {/* Recent Employees Table */}
        <div className="card">
          <div className="p-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <h3 className="text-lg font-semibold text-gray-900">Recent Employees</h3>
              <Link href="/hr/employees" className="text-blue-600 hover:text-blue-700 text-sm font-medium">
                View All →
              </Link>
            </div>
          </div>

          {/* Search and Filters */}
          <div className="p-6 border-b border-gray-200 bg-gray-50">
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1 relative">
                <MagnifyingGlassIcon className="h-5 w-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
                <input
                  type="text"
                  placeholder="Search employees..."
                  className="input pl-10"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <div className="flex gap-4">
                <select
                  className="select"
                  value={selectedStatus}
                  onChange={(e) => setSelectedStatus(e.target.value)}
                >
                  <option value="all">All Status</option>
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                  <option value="ON_LEAVE">On Leave</option>
                </select>
                <button className="btn-outline">
                  <AdjustmentsHorizontalIcon className="h-4 w-4 mr-2" />
                  Filters
                </button>
              </div>
            </div>
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            <table className="table">
              <thead className="table-header">
                <tr>
                  <th className="table-head">Employee</th>
                  <th className="table-head">Department</th>
                  <th className="table-head">Position</th>
                  <th className="table-head">Status</th>
                  <th className="table-head">Hire Date</th>
                  <th className="table-head">Actions</th>
                </tr>
              </thead>
              <tbody>
                {employeesLoading ? (
                  <tr>
                    <td colSpan={6} className="table-cell text-center py-8">
                      <div className="animate-spin h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
                    </td>
                  </tr>
                ) : filteredEmployees.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="table-cell text-center py-8 text-gray-500">
                      No employees found
                    </td>
                  </tr>
                ) : (
                  filteredEmployees.map((employee) => (
                    <tr key={employee.id} className="table-row">
                      <td className="table-cell">
                        <div>
                          <div className="font-medium text-gray-900">
                            {employee.firstName} {employee.lastName}
                          </div>
                          <div className="text-sm text-gray-500">{employee.email}</div>
                          <div className="text-sm text-gray-500">{employee.employeeCode}</div>
                        </div>
                      </td>
                      <td className="table-cell">
                        <span className="text-gray-900">{employee.departmentName}</span>
                      </td>
                      <td className="table-cell">
                        <span className="text-gray-900">{employee.positionTitle}</span>
                      </td>
                      <td className="table-cell">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(employee.status)}`}>
                          {employee.status}
                        </span>
                      </td>
                      <td className="table-cell">
                        <span className="text-gray-900">
                          {new Date(employee.hireDate).toLocaleDateString()}
                        </span>
                      </td>
                      <td className="table-cell">
                        <div className="flex space-x-2">
                          <button className="text-blue-600 hover:text-blue-700 text-sm">View</button>
                          <button className="text-gray-600 hover:text-gray-700 text-sm">Edit</button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}