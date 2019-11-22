import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Class that implements the channel used by headquarters and space explorers to communicate.
 */
public class CommunicationChannel {

	private ArrayBlockingQueue<Message> headQuarterChannel;
	private ArrayBlockingQueue<Message> spaceExplorerChannel;
	private Semaphore semaphore;
	private float writing = -1;

	private static Message tempMessage;
	private static boolean even = true;

	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {

		this.headQuarterChannel = new ArrayBlockingQueue<Message>(1000);
		this.spaceExplorerChannel = new ArrayBlockingQueue<Message>(1000);
		this.semaphore = new Semaphore(1);
	}

	/**
	 * Puts a message on the space explorer channel (i.e., where space explorers write to and 
	 * headquarters read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageSpaceExplorerChannel(Message message) {

		try {
			spaceExplorerChannel.put(message);
		} catch (InterruptedException ignored) {}
	}

	/**
	 * Gets a message from the space explorer channel (i.e., where space explorers write to and
	 * headquarters read from).
	 * 
	 * @return message from the space explorer channel
	 */
	public Message getMessageSpaceExplorerChannel() {

		Message returnMsg = null;
		try {
			returnMsg = spaceExplorerChannel.take();
		} catch (InterruptedException ignored) {}

		return returnMsg;
	}

	/**
	 * Puts a message on the headquarters channel (i.e., where headquarters write to and 
	 * space explorers read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageHeadQuarterChannel(Message message){

		if(writing != Thread.currentThread().getId()) {
			try {
				semaphore.acquire();
			} catch (InterruptedException ignored) {}
		}
		writing = Thread.currentThread().getId();

		if (!message.getData().equals(HeadQuarter.END)) {

			if (even) {

				tempMessage = new Message(message.getCurrentSolarSystem(), 0, null);
				even = false;
				return;
			} else {

				tempMessage.setCurrentSolarSystem(message.getCurrentSolarSystem());
				tempMessage.setData(message.getData());
				even = true;
			}

			try {
				headQuarterChannel.put(tempMessage);

			} catch (InterruptedException ignored) {}
		} else {

			writing = -1;
			semaphore.release();
		}
	}

	/**
	 * Gets a message from the headquarters channel (i.e., where headquarters write to and
	 * space explorer read from).
	 * 
	 * @return message from the header quarter channel
	 */
	public Message getMessageHeadQuarterChannel() {

		Message returnMsg = null;
		try {
			returnMsg = headQuarterChannel.take();
		} catch (InterruptedException ignored) {}

		return returnMsg;
	}
}
