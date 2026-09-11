import { useParams } from 'react-router-dom';
import { TicketDetail } from '../components/TicketDetail';

export function TicketDetailPage() {
  const { id } = useParams<{ id: string }>();

  if (!id) {
    return null;
  }

  return <TicketDetail ticketId={id} />;
}
