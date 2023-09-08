/**
 * Communication bus for easy information exchange in a system.
 * 
 * <p>
 * The Application Bus is an easy to use component, which allows different components to communicate
 * to each other without knownledge of the other component. The idea results of the fact, that one
 * component doesn't have to know, who has called it. The important points are the parameters of the
 * call.
 * </p>
 * 
 * <p>
 * The package contains some important classes and some helpers:
 * </p>
 * <ul>
 * <li><a href="#Bus">Bus</a >: The communication bus itself.</li>
 * <li><a href="#Service">Service</a >: The service, a message belongs to.</li>
 * <li><a href="#Sender">Sender</a >: The sender of a message.</li>
 * <li><a href="#AbstractReceiver">Receiver</a >: The receiver of a message.</li>
 * </ul>
 * <hr />
 * 
 * <h2><a name="Bus">The Communication {@link com.top_logic.event.bus.Bus}</a></h2>
 * 
 * <p>
 * The communication bus is the core component of this system. It organizes the communication
 * between the differnt senders and receviers. There is only one bus in the system (VM) and you can
 * access it with the following call:
 * </p>
 * 
 * <pre>
 * Bus theBus = Bus.getInstance();
 * </pre>
 * 
 * <p>
 * If you got an instance of the bus, you can either attach yourself as sender
 * ({@link com.top_logic.event.bus.Bus#addSender addSender}) or receiver
 * ({@link com.top_logic.event.bus.Bus#subscribe subscribe}). If you are a receiver, you can also
 * remove yourself from the bus using the {@link com.top_logic.event.bus.Bus#unsubscribe
 * unsubscribe} method.
 * </p>
 * <p>
 * The bus has several channels, which can be subscribed and on which you can write messages to.
 * These channels are identified by so called services, which will be explained next.
 * </p>
 * <hr />
 * 
 * <h2><a name="Service">The {@link com.top_logic.event.bus.Service}</a></h2>
 * 
 * <p>
 * The service identifies channels on the bus, whereby the service can define one channel or a set
 * of channels. Therefore the service knows two parameters, the <code>namespace</code> and the
 * <code>name</code>. The first should be used to identify areas of simalar messages (e.g. "Movies"
 * or "News"). The second parameter defines the specific name or the channel (e.g. "cnn" or "n-tv").
 * </p>
 * <p>
 * If you are interested on a specific channel, you can construct a service describing that channel:
 * </p>
 * 
 * <pre>
 * Service theService = new Service("News", "cnn");
 * Receiver theRecv = new Receiver();
 * 
 * Bus.subscribe(theRecv, theService);
 * </pre>
 * <p>
 * This will send all information published to the channel ("News", "cnn") to the receiver
 * <code>theRecv</code>.
 * </p>
 * <p>
 * When you are interested in all information of a specific namespace, you can use a constant for
 * the name:
 * </p>
 * 
 * <pre>
 * Service theService = new Service("News", Service.WILDCARD);
 * Receiver theRecv = new Receiver();
 * 
 * Bus.subscribe(theRecv, theService);
 * </pre>
 * <p>
 * This will send all information published to the channel with the namespace "News".
 * </p>
 * <hr />
 * 
 * <h2><a name="Sender">The {@link com.top_logic.event.bus.Sender} of Information</a></h2>
 * 
 * <p>
 * When you want to publish an information on the bus, simply use the
 * {@link com.top_logic.event.bus.Sender} class. That class provides a method for sending objects
 * through the bus.
 * </p>
 * <p>
 * When sending messages you should use the {@link com.top_logic.event.bus.BusEvent}, which
 * encapsulates the needed information of the message send:
 * </p>
 * 
 * <pre>
 * Sender theSender = new Sender("News", "cnn");
 * BusEvent theEvent = new BusEvent(theSender,
 * 	theSender.getService(),
 * 	BusEvent.DELETED,
 * 	"Deleted something");
 * 
 * Bus.getInstance().addSender(theSender);
 * theSender.send(theEvent);
 * </pre>
 * <p>
 * This will send the message "Deleted something" to the channel ("News", "cnn"). All receivers on
 * that channel will receive this message.
 * </p>
 * <hr />
 * 
 * <h2><a name="AbstractReceiver">The {@link com.top_logic.event.bus.AbstractReceiver} of
 * Information</a></h2>
 * 
 * <p>
 * A receiver get all messages send to the defined channel. The method of submitting a message runs
 * in a separate thread, so the sender will not know, when the sending has finished. That thread
 * send the message to the receiver and waits for its return. If this call throws an exeception,
 * it'll be catched. All receivers will be notified, but the time when this will happen is not
 * guaranteed.
 * </p>
 * <p>
 * If you want to implement an own receiver, you can use the default implementation. This will
 * automatically attach to the bus to receive the information. In your implementation you should
 * overwrite the receiver() method, because the default behavoir writes every message to the
 * stdout.:
 * </p>
 * 
 * <pre>
 * public class MyReceiver extends Receiver {
 * 
 * 	public MyReceiver() {
 * 		super();
 * 	}
 * 
 * 	public MyReceiver(Service aService) {
 * 		super(aService);
 * 	}
 * 
 * 	public void receive(BusEvent anEvent) {
 * 		// do something with event...
 * 	}
 * }
 * </pre>
 */
package com.top_logic.event.bus;
