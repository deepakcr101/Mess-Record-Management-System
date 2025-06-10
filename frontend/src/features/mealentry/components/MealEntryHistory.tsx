// src/features/mealentry/components/MealEntryHistory.tsx
import React, { useEffect, useState } from 'react';
import { mealEntryService } from '../services/mealEntryService';
import type { MealEntryResponseData } from '../../../types/mealEntry';
import type { Page } from '../../../types/common';
import { useAuth } from '../../../context/AuthContext';

const MealEntryHistory: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [historyPage, setHistoryPage] = useState<Page<MealEntryResponseData> | null>(null);
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const PAGE_SIZE = 5; // Number of items per page

  const fetchHistory = async (page: number) => {
    if (!isAuthenticated) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await mealEntryService.getMyHistory(page, PAGE_SIZE);
      setHistoryPage(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load meal entry history.');
      console.error("Error fetching meal entry history:", err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory(currentPage);
  }, [isAuthenticated, currentPage]); // Refetch when auth status or current page changes

  if (isLoading) {
    return <p>Loading meal history...</p>;
  }
  if (error) {
    return <p style={{ color: 'red' }}>Error: {error}</p>;
  }
  if (!historyPage || historyPage.empty) {
    return <p>No meal entry history found.</p>;
  }

  // Basic Styling
  const tableStyle: React.CSSProperties = { width: '100%', borderCollapse: 'collapse', marginTop: '10px' };
  const thStyle: React.CSSProperties = { background: '#f2f2f2', padding: '8px', border: '1px solid #ddd', textAlign: 'left' };
  const tdStyle: React.CSSProperties = { padding: '8px', border: '1px solid #ddd', textAlign: 'left' };
  const paginationStyle: React.CSSProperties = { marginTop: '15px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' };
  const buttonStyle: React.CSSProperties = { padding: '5px 10px', cursor: 'pointer' };

  return (
    <div>
      <h4>Your Meal Entry History</h4>
      <table style={tableStyle}>
        <thead>
          <tr>
            <th style={thStyle}>Date</th>
            <th style={thStyle}>Time</th>
            <th style={thStyle}>Meal Type</th>
          </tr>
        </thead>
        <tbody>
          {historyPage.content.map(entry => (
            <tr key={entry.entryId}>
              <td style={tdStyle}>{entry.entryDate}</td>
              <td style={tdStyle}>{entry.entryTime}</td>
              <td style={tdStyle}>{entry.mealType}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div style={paginationStyle}>
        <button
          onClick={() => setCurrentPage(prev => prev - 1)}
          disabled={historyPage.first}
          style={buttonStyle}
        >
          Previous
        </button>
        <span>
          Page {historyPage.number + 1} of {historyPage.totalPages}
        </span>
        <button
          onClick={() => setCurrentPage(prev => prev + 1)}
          disabled={historyPage.last}
          style={buttonStyle}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default MealEntryHistory;