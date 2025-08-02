'use client';

import { useState } from 'react';
import Link from 'next/link';
import { 
  ChartBarIcon, 
  UsersIcon, 
  CurrencyDollarIcon,
  CubeIcon,
  ShoppingCartIcon,
  ClipboardDocumentListIcon,
  Cog6ToothIcon
} from '@heroicons/react/24/outline';

const modules = [
  {
    id: 'hr',
    name: 'Human Resources',
    description: 'Manage employees, departments, and HR operations',
    icon: UsersIcon,
    href: '/hr',
    color: 'bg-blue-500',
    stats: { value: '156', label: 'Active Employees' }
  },
  {
    id: 'finance',
    name: 'Finance',
    description: 'Financial management and accounting',
    icon: CurrencyDollarIcon,
    href: '/finance',
    color: 'bg-green-500',
    stats: { value: '$2.4M', label: 'Monthly Revenue' }
  },
  {
    id: 'inventory',
    name: 'Inventory',
    description: 'Stock management and warehouse operations',
    icon: CubeIcon,
    href: '/inventory',
    color: 'bg-purple-500',
    stats: { value: '2,431', label: 'Items in Stock' }
  },
  {
    id: 'sales',
    name: 'Sales',
    description: 'Customer management and sales tracking',
    icon: ShoppingCartIcon,
    href: '/sales',
    color: 'bg-orange-500',
    stats: { value: '89', label: 'Active Deals' }
  },
  {
    id: 'purchase',
    name: 'Purchase',
    description: 'Procurement and supplier management',
    icon: ClipboardDocumentListIcon,
    href: '/purchase',
    color: 'bg-indigo-500',
    stats: { value: '23', label: 'Pending Orders' }
  },
  {
    id: 'production',
    name: 'Production',
    description: 'Manufacturing and production planning',
    icon: Cog6ToothIcon,
    href: '/production',
    color: 'bg-red-500',
    stats: { value: '12', label: 'Active Orders' }
  }
];

export default function HomePage() {
  const [hoveredModule, setHoveredModule] = useState<string | null>(null);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <ChartBarIcon className="h-8 w-8 text-primary-600" />
              <h1 className="ml-3 text-2xl font-bold text-gray-900">ERP System</h1>
            </div>
            <div className="flex items-center space-x-4">
              <button className="btn-ghost">Profile</button>
              <button className="btn-primary">Settings</button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Welcome Section */}
        <div className="text-center mb-12">
          <h2 className="text-4xl font-bold text-gray-900 mb-4">
            Welcome to Your ERP Dashboard
          </h2>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto">
            Manage all aspects of your business with our comprehensive enterprise resource planning system.
            Choose a module below to get started.
          </p>
        </div>

        {/* Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-12">
          <div className="card p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 rounded-lg">
                <UsersIcon className="h-8 w-8 text-blue-600" />
              </div>
              <div className="ml-4">
                <p className="text-2xl font-bold text-gray-900">156</p>
                <p className="text-gray-600">Total Employees</p>
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 rounded-lg">
                <CurrencyDollarIcon className="h-8 w-8 text-green-600" />
              </div>
              <div className="ml-4">
                <p className="text-2xl font-bold text-gray-900">$2.4M</p>
                <p className="text-gray-600">Monthly Revenue</p>
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center">
              <div className="p-2 bg-purple-100 rounded-lg">
                <CubeIcon className="h-8 w-8 text-purple-600" />
              </div>
              <div className="ml-4">
                <p className="text-2xl font-bold text-gray-900">2,431</p>
                <p className="text-gray-600">Inventory Items</p>
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center">
              <div className="p-2 bg-orange-100 rounded-lg">
                <ShoppingCartIcon className="h-8 w-8 text-orange-600" />
              </div>
              <div className="ml-4">
                <p className="text-2xl font-bold text-gray-900">89</p>
                <p className="text-gray-600">Active Deals</p>
              </div>
            </div>
          </div>
        </div>

        {/* Modules Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {modules.map((module) => {
            const IconComponent = module.icon;
            return (
              <Link
                key={module.id}
                href={module.href}
                className="group block"
                onMouseEnter={() => setHoveredModule(module.id)}
                onMouseLeave={() => setHoveredModule(null)}
              >
                <div className={`card p-8 h-full transition-all duration-300 hover:shadow-lg hover:-translate-y-1 ${
                  hoveredModule === module.id ? 'shadow-lg -translate-y-1' : ''
                }`}>
                  <div className="flex items-center mb-6">
                    <div className={`p-3 rounded-lg ${module.color}`}>
                      <IconComponent className="h-8 w-8 text-white" />
                    </div>
                    <div className="ml-4">
                      <h3 className="text-xl font-bold text-gray-900 group-hover:text-primary-600 transition-colors">
                        {module.name}
                      </h3>
                    </div>
                  </div>
                  
                  <p className="text-gray-600 mb-6 leading-relaxed">
                    {module.description}
                  </p>
                  
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-2xl font-bold text-gray-900">
                        {module.stats.value}
                      </p>
                      <p className="text-sm text-gray-500">
                        {module.stats.label}
                      </p>
                    </div>
                    <div className="text-primary-600 group-hover:text-primary-700 transition-colors">
                      <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                      </svg>
                    </div>
                  </div>
                </div>
              </Link>
            );
          })}
        </div>

        {/* Recent Activity */}
        <div className="mt-16">
          <h3 className="text-2xl font-bold text-gray-900 mb-8">Recent Activity</h3>
          <div className="card">
            <div className="p-6">
              <div className="space-y-4">
                <div className="flex items-center p-4 bg-gray-50 rounded-lg">
                  <div className="p-2 bg-blue-100 rounded-full">
                    <UsersIcon className="h-5 w-5 text-blue-600" />
                  </div>
                  <div className="ml-4">
                    <p className="font-medium text-gray-900">New employee John Doe added</p>
                    <p className="text-sm text-gray-500">2 hours ago</p>
                  </div>
                </div>
                <div className="flex items-center p-4 bg-gray-50 rounded-lg">
                  <div className="p-2 bg-green-100 rounded-full">
                    <CurrencyDollarIcon className="h-5 w-5 text-green-600" />
                  </div>
                  <div className="ml-4">
                    <p className="font-medium text-gray-900">Invoice #INV-2024-001 paid</p>
                    <p className="text-sm text-gray-500">4 hours ago</p>
                  </div>
                </div>
                <div className="flex items-center p-4 bg-gray-50 rounded-lg">
                  <div className="p-2 bg-purple-100 rounded-full">
                    <CubeIcon className="h-5 w-5 text-purple-600" />
                  </div>
                  <div className="ml-4">
                    <p className="font-medium text-gray-900">Low stock alert for Product ABC</p>
                    <p className="text-sm text-gray-500">6 hours ago</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}