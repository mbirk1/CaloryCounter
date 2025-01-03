import './styles/App.css';
import {HashRouter, Routes, Route} from 'react-router-dom';
import Navigation from './components/Navigation'
import Dashboard from './components/pages/Dashboard';
import Food from './components/pages/Food';
import Recipe from './components/pages/Recipe';


function App() {
  return (
      <div className="App">
      <HashRouter>
        <header>
          <Navigation />
        </header>
      
        <Routes>
          <Route path='/' index element={<Dashboard />}/>
          <Route path='/food' element={<Food />} />
          <Route path='/recipe' element={<Recipe />} />
          <Route path="*" element={<Dashboard />} />
        </Routes>
      </HashRouter>
    </div>
  );
}

export default App;
