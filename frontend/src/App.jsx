import React, { Suspense } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import PrivateRoute from './components/PrivateRoute'
import Navbar from './components/Navbar'
import Footer from './components/Footer'

const Login = React.lazy(() => import('./pages/Login'))
const Register = React.lazy(() => import('./pages/Register'))
const Home = React.lazy(() => import('./pages/Home'))
const MySnippets = React.lazy(() => import('./pages/MySnippets'))
const CreateSnippet = React.lazy(() => import('./pages/CreateSnippet'))
const EditSnippet = React.lazy(() => import('./pages/EditSnippet'))
const ViewSnippet = React.lazy(() => import('./pages/ViewSnippet'))
const SharedSnippetView = React.lazy(() => import('./pages/SharedSnippetView'))
const Search = React.lazy(() => import('./pages/Search'))
const UserProfile = React.lazy(() => import('./pages/UserProfile'))
const StarredSnippets = React.lazy(() => import('./pages/StarredSnippets'))

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="min-h-screen bg-gray-950 text-gray-100 flex flex-col justify-between">
          <div className="flex-1">
            <Navbar />
            <Suspense fallback={
              <div className="text-center text-gray-500 py-16">
                Loading Page...
              </div>
            }>
              <Routes>
                <Route path="/" element={<Navigate to="/home" replace />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/home" element={<Home />} />
                <Route path="/search" element={<Search />} />
                <Route path="/snippet/:id" element={<ViewSnippet />} />
                <Route path="/share/snippet/:token" element={<SharedSnippetView />} />
                <Route path="/user/:username" element={<UserProfile />} />
                <Route path="/my-snippets" element={
                  <PrivateRoute><MySnippets /></PrivateRoute>
                } />
                <Route path="/starred" element={
                  <PrivateRoute><StarredSnippets /></PrivateRoute>
                } />
                <Route path="/create" element={
                  <PrivateRoute><CreateSnippet /></PrivateRoute>
                } />
                <Route path="/edit/:id" element={
                  <PrivateRoute><EditSnippet /></PrivateRoute>
                } />
              </Routes>
            </Suspense>
          </div>
          <Footer />
        </div>
      </BrowserRouter>
    </AuthProvider>
  )
}
