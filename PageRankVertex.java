import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class PageRankVertex extends Vertex<Double, Double, Void>  implements Serializable, Comparable<PageRankVertex>{

	public PageRankVertex(int vertex_id, Double vertex_value) {
		super(vertex_id, vertex_value);
	}

	@Override
	public boolean compute(List<String> workerIDList) {
		boolean changed = false;
		if (supersteps >= 1 && supersteps < 10) {
			Message<Double> message;
			double sum = 0.0;
			while (true) {
				synchronized (messageList) {
					message = messageList.peek();
					if (message == null || message.getSuperstep() != supersteps)
						break;
					sum += message.getValue();
					changed = true;
					messageList.poll();
				}
			}
			if(changed)
				value = 0.15 + 0.85 * sum;

		}
		if (supersteps < 10) {
			int n = outEdgeList.size();
			for (Edge edge : outEdgeList) {
				try {
					Socket socket = new Socket(workerIDList.get(edge.getTarget() % workerIDList.size()), 9000);
					ObjectOutput sout = new ObjectOutputStream(socket.getOutputStream());
					sout.writeObject(
							new Message<Double>("neighborMessage", value / n, edge.getTarget(), supersteps + 1));
					sout.flush();
					sout.close();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		supersteps++;

		if(supersteps==1)
			return true;
		return changed;

	}



	@Override
	public int compareTo(PageRankVertex o) {

		double compareValue = o.value;

		if(value < compareValue)
			return 1;
		else if(value > compareValue)
			return -1;
		else
			return 0;
	}

}
