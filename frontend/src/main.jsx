import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
import {
  createBrowserRouter,
  RouterProvider,
} from "react-router-dom";

import ErrorPage from './error-page'

import Root from './routes/root'

import Index from './routes/index';

import SignUp, {
  action as signUpAction
} from './routes/signUp/SignUp';

import Users, { 
  loader as usersLoader
} from './routes/users/users';

import SignIn from './routes/signIn/SignIn.jsx';

const router = createBrowserRouter([
  {
    path: "/",
    element: <Root/>,
    errorElement: <ErrorPage/>,
    children: [
      {
        errorElement: <ErrorPage/>,
        children: [
          {
            path: "login",
            element: <SignIn/>
          },
          {
            path: "users",
            element: <Users/>,
            loader: usersLoader
          },
          {
            path: "register",
            element: <SignUp/>,
            action: signUpAction,
          },   
          {
            index: true,
            element: <Index/>,
          },   
        ]
      }
    ],
  },
]);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
)
