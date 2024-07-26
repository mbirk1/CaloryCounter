import './styles/App.css';
import ReactDOM from "react-dom/client";
import {BrowserRouter, Routes, Route} from 'react-router-dom';
import Dashboard from './components/Dashboard';
import Food from './components/Food';
import Recipe from './components/Recipe';
import Navigation from './components/Navigation'


function App() {
  return (
      <div className="App">
      <header>
        <Navigation />
      </header>
      <BrowserRouter>
        <Routes>
          <Route path='/' index element={<Dashboard />}/>
          <Route path='/food' element={<Food />} />
          <Route path='/recipe' element={<Recipe />} />
          <Route path="*" element={<Dashboard />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
