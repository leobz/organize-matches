import { Outlet, NavLink, useNavigation } from 'react-router-dom';

export default function Root() {
  const navigation = useNavigation();

  return (
    <>
      <div id="sidebar">
        <h1>Organize Matches</h1>
        <nav>
          <ul>
            <li key='login'>
              <NavLink to='home'> Home </NavLink>
              <NavLink to='login'> Sign In </NavLink>
              <NavLink to='register'> Sign Up </NavLink>
              <NavLink to='users'> Users </NavLink>
            </li>
              <li key="sections">
                  <NavLink to="matches">Matches</NavLink>
              </li>
          </ul>
        </nav>
      </div>
      <div 
        id="detail"
        className={
          navigation.state === "loading" ? "loading" : ""
        }
      >
        <Outlet/>
      </div>
    </>
  );
}
