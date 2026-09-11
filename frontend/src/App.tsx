import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { TicketList } from './components/TicketList';
import { CreateTicketPage } from './pages/CreateTicketPage';
import { TicketDetailPage } from './pages/TicketDetailPage';

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<TicketList />} />
        <Route path="tickets/new" element={<CreateTicketPage />} />
        <Route path="tickets/:id" element={<TicketDetailPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
