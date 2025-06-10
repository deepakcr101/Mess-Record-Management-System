// src/features/subscriptions/components/SubscriptionPanel.tsx

import React, { useEffect, useState } from 'react';
import { subscriptionService } from '../services/subscriptionService';
// 👇 Ensure both the type AND the enum object are imported
import { type MySubscriptionStatusDTO, SubscriptionStatus } from '../../../types/subscription';
import { useAuth } from '../../../context/AuthContext';

// IMPORTANT: Store your Stripe Publishable Key in your frontend's .env file
// Create a file named .env.local in the root of your React project and add:
// VITE_STRIPE_PUBLISHABLE_KEY=pk_test_xxxxxxxxxxxxxxxxxxxxxxxxxx
const STRIPE_PUBLISHABLE_KEY = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY;

// IMPORTANT: Store your Stripe Price ID here as well
// VITE_STRIPE_PRICE_ID=price_xxxxxxxxxxxxxxxxxxxx
const STRIPE_PRICE_ID = import.meta.env.VITE_STRIPE_PRICE_ID;


const SubscriptionPanel: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [subscription, setSubscription] = useState<MySubscriptionStatusDTO | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true); // Start as true to show initial loading state
  const [error, setError] = useState<string | null>(null);
  const [isRedirecting, setIsRedirecting] = useState<boolean>(false);

  useEffect(() => {
    const fetchSubscriptionStatus = async () => {
      if (!isAuthenticated) {
        setIsLoading(false); // Stop loading if user is not logged in
        return;
      }
      setIsLoading(true);
      setError(null);
      try {
        const data = await subscriptionService.getMySubscriptionStatus();
        setSubscription(data);
      } catch (err: any) {
        setError(err.message || 'Failed to load subscription status.');
        console.error("Error fetching subscription status:", err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchSubscriptionStatus();
  }, [isAuthenticated]); // Re-fetches when authentication status changes

  const handlePurchaseSubscription = async () => {
    if (!STRIPE_PUBLISHABLE_KEY || !STRIPE_PRICE_ID) {
        alert("Stripe is not configured correctly. Publishable key or Price ID is missing.");
        console.error("Stripe keys missing. Check your .env.local file for VITE_STRIPE_PUBLISHABLE_KEY and VITE_STRIPE_PRICE_ID.");
        return;
    }
    setIsRedirecting(true);
    setError(null);
    try {
      // Send the correct payload with the stripePriceId
      const sessionId = await subscriptionService.purchaseSubscription({ stripePriceId: STRIPE_PRICE_ID });
      
      console.log("Stripe Session ID for redirect:", sessionId);

      // TODO: Implement actual Stripe redirect.
      // This requires loading Stripe.js, for example, by adding the script to your public/index.html
      // or by using the official @stripe/stripe-js library.
      const stripe = (window as any).Stripe(STRIPE_PUBLISHABLE_KEY);
      if (stripe && sessionId) {
        // This will redirect your user to the Stripe Checkout page
        const { error } = await stripe.redirectToCheckout({ sessionId: sessionId });
        if (error) {
            console.error("Stripe redirection error:", error);
            setError(error.message);
        }
      } else {
        console.error('Stripe.js not loaded or no session ID returned from backend.');
        alert('Error initiating payment. Stripe.js not found.');
      }

    } catch (err: any) {
      setError(err.message || 'Failed to initiate subscription purchase.');
      console.error("Error purchasing subscription:", err);
    } finally {
      setIsRedirecting(false);
    }
  };


  // --- Render Logic ---

  if (isLoading) {
    return <p>Loading subscription status...</p>;
  }

  if (error) {
    return <p style={{ color: 'red' }}>Error: {error}</p>;
  }

  // This is the CRITICAL FIX for the "Cannot read properties of null" error.
  // After loading and with no errors, if subscription is still null, it means something went wrong with the data fetch.
  if (!subscription) {
    return <p>Could not retrieve subscription information. Please try again later.</p>;
  }
  
  // Styling can be moved to a CSS file for better organization
  const panelStyle: React.CSSProperties = { border: '1px solid #007bff', padding: '15px', margin: '20px 0', borderRadius: '5px', backgroundColor: '#f0f8ff' };
  const statusStyle = (status: SubscriptionStatus | 'NO_SUBSCRIPTION_HISTORY'): React.CSSProperties => ({
      fontWeight: 'bold',
      color: status === SubscriptionStatus.ACTIVE ? 'green' : (status === SubscriptionStatus.EXPIRED || status === SubscriptionStatus.CANCELLED ? 'red' : 'orange'),
  });
  const buttonStyle: React.CSSProperties = { padding: '10px 15px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginTop: '10px',  opacity: isRedirecting ? 0.7 : 1 };


  return (
    <div style={panelStyle}>
      <h4>Your Subscription Status</h4>
      {subscription.status !== SubscriptionStatus.NO_SUBSCRIPTION_HISTORY ? (
        // Case 1: User has a subscription history (Active, Expired, etc.)
        <div>
          <p>Status: <span style={statusStyle(subscription.status)}>{subscription.status}</span></p>
          <p>Plan: {subscription.planName || 'N/A'}</p>
          <p>Subscription Period: {subscription.startDate} to {subscription.endDate}</p>
          
          {subscription.status !== SubscriptionStatus.ACTIVE && (
            <button onClick={handlePurchaseSubscription} disabled={isRedirecting} style={buttonStyle}>
              {isRedirecting ? 'Processing...' : 'Subscribe / Renew Now'}
            </button>
          )}
        </div>
      ) : (
        // Case 2: User has no subscription history
        <div>
          <p>You do not have an active subscription.</p>
          <button onClick={handlePurchaseSubscription} disabled={isRedirecting} style={buttonStyle}>
            {isRedirecting ? 'Processing...' : 'Subscribe Now (₹3500/month)'}
          </button>
        </div>
      )}
    </div>
  );
};

export default SubscriptionPanel;