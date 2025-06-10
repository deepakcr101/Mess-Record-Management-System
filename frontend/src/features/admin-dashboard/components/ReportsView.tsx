// src/features/admin-dashboard/components/ReportsView.tsx
import React, { useEffect, useState } from 'react';
import { reportService } from '../services/reportService';
import { Grid, Paper, Typography, CircularProgress, Alert, Box } from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import SubscriptionsIcon from '@mui/icons-material/Subscriptions';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'; // Import chart components

const StatCard: React.FC<{ title: string; value: string | number; icon: React.ReactNode }> = ({ title, value, icon }) => (
    <Paper elevation={3} sx={{ p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box>
            <Typography color="text.secondary" gutterBottom>{title}</Typography>
            <Typography variant="h4">{value}</Typography>
        </Box>
        <Box sx={{ color: 'primary.main' }}>
            {icon}
        </Box>
    </Paper>
);

const ReportsView: React.FC = () => {
    const [studentCount, setStudentCount] = useState<number | null>(null);
    const [activeSubsCount, setActiveSubsCount] = useState<number | null>(null);
    const [salesData, setSalesData] = useState<any[]>([]); // For chart
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchAllReports = async () => {
            setIsLoading(true);
            setError(null);
            try {
                // Fetch all reports in parallel
                const [studentCountRes, activeSubsRes, salesSummaryRes] = await Promise.all([
                    reportService.getTotalStudentCount(),
                    reportService.getActiveSubscriptionCount(),
                    // For sales summary, let's fetch for the last 30 days by default
                    reportService.getSalesSummary(
                        new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
                        new Date().toISOString().split('T')[0]
                    )
                ]);

                setStudentCount(studentCountRes.totalStudents);
                setActiveSubsCount(activeSubsRes.activeSubscriptions);
                // Format data for chart
                setSalesData([
                    { name: 'Total Students', value: studentCountRes.totalStudents },
                    { name: 'Active Subscriptions', value: activeSubsRes.activeSubscriptions },
                ]);

            } catch (err: any) {
                setError(err.message || 'Failed to load reports.');
                console.error("Error fetching reports:", err);
            } finally {
                setIsLoading(false);
            }
        };

        fetchAllReports();
    }, []); // Fetch once on component mount

    if (isLoading) {
        return <CircularProgress />;
    }

    if (error) {
        return <Alert severity="error">{error}</Alert>;
    }

    return (
        <Box sx={{ width: '100%', marginTop: 4 }}>
            <h3>Reports & Analytics</h3>
            <Grid container spacing={3}>
                {/* Stat Cards */}
                <Grid item xs={12} sm={6} md={4}>
                    <StatCard title="Total Students" value={studentCount ?? 'N/A'} icon={<PeopleIcon sx={{ fontSize: 40 }} />} />
                </Grid>
                <Grid item xs={12} sm={6} md={4}>
                    <StatCard title="Active Subscriptions" value={activeSubsCount ?? 'N/A'} icon={<SubscriptionsIcon sx={{ fontSize: 40 }} />} />
                </Grid>
                {/* Add more stat cards here, e.g., for sales summary */}

                {/* Chart */}
                <Grid item xs={12} md={8}>
                     <Paper elevation={3} sx={{ p: 2, height: 300 }}>
                        <Typography variant="h6" gutterBottom>Overview</Typography>
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={salesData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="name" />
                                <YAxis />
                                <Tooltip />
                                <Legend />
                                <Bar dataKey="value" fill="#8884d8" />
                            </BarChart>
                        </ResponsiveContainer>
                     </Paper>
                </Grid>
            </Grid>
        </Box>
    );
};

export default ReportsView;