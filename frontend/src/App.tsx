import './styles/App.css';
import ReactDOM from "react-dom/client";
import {BrowserRouter, Routes, Route} from 'react-router-dom';
import Navigation from './components/Navigation'
import Dashboard from './components/pages/Dashboard';
import Food from './components/pages/Food';
import Recipe from './components/pages/Recipe';


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
