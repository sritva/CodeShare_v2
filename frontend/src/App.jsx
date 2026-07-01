import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import PrivateRoute from './components/PrivateRoute'
import Navbar from './components/Navbar'
import Login from './pages/Login'
import Register from './pages/Register'
import Home from './pages/Home'
import MySnippets from './pages/MySnippets'
import CreateSnippet from './pages/CreateSnippet'
import EditSnippet from './pages/EditSnippet'
import ViewSnippet from './pages/ViewSnippet'
import SharedSnippetView from './pages/SharedSnippetView'
import Search from './pages/Search'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="min-h-screen bg-gray-950 text-gray-100">
          <Navbar />
          <Routes>
            <Route path="/" element={<Navigate to="/home" replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/home" element={<Home />} />
            <Route path="/search" element={<Search />} />
            <Route path="/snippet/:id" element={<ViewSnippet />} />
            <Route path="/share/snippet/:token" element={<SharedSnippetView />} />
            <Route path="/my-snippets" element={
              <PrivateRoute><MySnippets /></PrivateRoute>
            } />
            <Route path="/create" element={
              <PrivateRoute><CreateSnippet /></PrivateRoute>
            } />
            <Route path="/edit/:id" element={
              <PrivateRoute><EditSnippet /></PrivateRoute>
            } />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  )
}
