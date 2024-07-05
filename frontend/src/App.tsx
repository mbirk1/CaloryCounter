import React from 'react';
import logo from './static/logo.svg';
import './styles/App.css';
import Dashboard from './components/Dashboard';
import Navigation from './components/Navigation'


function App() {
  return (
      <div className="App">
      <header>
        <Navigation />
      </header>
        <Dashboard />
    </div>
  );
}

export default App;
