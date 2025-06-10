// src/features/purchase/components/PurchaseHistory.tsx
import React, { useEffect, useState } from 'react';
import { purchaseService } from '../services/purchaseService';
import type { PurchaseResponseDTO } from '../../../types/purchase';
import type { Page } from '../../../types/common';
import { useAuth } from '../../../context/AuthContext';

const PurchaseHistory: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [historyPage, setHistoryPage] = useState<Page<PurchaseResponseDTO> | null>(null);
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const PAGE_SIZE = 5;

  const fetchHistory = async (page: number) => {
    if (!isAuthenticated) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await purchaseService.getMyPurchaseHistory(page, PAGE_SIZE);
      setHistoryPage(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load purchase history.');
      console.error("Error fetching purchase history:", err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory(currentPage);
  }, [isAuthenticated, currentPage]);

  if (isLoading) {
    return <p>Loading purchase history...</p>;
  }
  if (error) {
    return <p style={{ color: 'red' }}>Error: {error}</p>;
  }
  if (!historyPage || historyPage.empty) {
    return <p>No purchase history found.</p>;
  }

  // Re-using styles from MealEntryHistory for consistency
  const tableStyle: React.CSSProperties = { width: '100%', borderCollapse: 'collapse', marginTop: '10px' };
  const thStyle: React.CSSProperties = { background: '#f2f2f2', padding: '8px', border: '1px solid #ddd', textAlign: 'left' };
  const tdStyle: React.CSSProperties = { padding: '8px', border: '1px solid #ddd', textAlign: 'left' };
  const paginationStyle: React.CSSProperties = { marginTop: '15px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' };
  const buttonStyle: React.CSSProperties = { padding: '5px 10px', cursor: 'pointer' };

  return (
    <div>
      <h4>Your Individual Dish Purchase History</h4>
      <table style={tableStyle}>
        <thead>
          <tr>
            <th style={thStyle}>Date</th>
            <th style={thStyle}>Item Name</th>
            <th style={thStyle}>Quantity</th>
            <th style={thStyle}>Total Amount</th>
          </tr>
        </thead>
        <tbody>
          {historyPage.content.map(purchase => (
            <tr key={purchase.purchaseId}>
              <td style={tdStyle}>{new Date(purchase.purchaseDate).toLocaleString()}</td>
              <td style={tdStyle}>{purchase.menuItem.name}</td>
              <td style={tdStyle}>{purchase.quantity}</td>
              <td style={tdStyle}>₹{purchase.totalAmount.toFixed(2)}</td>
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

export default PurchaseHistory;