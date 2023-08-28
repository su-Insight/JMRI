package jmri.jmrix.mqtt;

import java.util.concurrent.LinkedBlockingQueue;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.util.regex.*;
import jmri.ThrottleListener;


/**
 * An implementation of AbstractThrottle with code specific to a MQTT
 * connection.
 *
 * @author Dean Cording (C) 2023
 */


 public class MqttThrottle extends AbstractThrottle implements MqttEventListener{

    private final MqttAdapter mqttAdapter;
    @Nonnull
    public String sendThrottleTopic = "cab/$address/throttle";
    @Nonnull
    public String rcvThrottleTopic ="cab/$address/throttle";
    @Nonnull
    public String sendDirectionTopic = "cab/$address/direction";
    @Nonnull
    public String rcvDirectionTopic = "cab/$address/direction";
    @Nonnull
    public String sendFunctionTopic = "cab/$address/function/$function";
    @Nonnull
    public String rcvFunctionTopic = "cab/$address/function/$function";

    protected int address = -1;

    private Pattern functionPattern;

    private MqttConsistManager consistManager;

   /**
     * Constructor.
     * @param memo system connection.
     */

    public MqttThrottle(MqttSystemConnectionMemo memo) {
        super(memo);
        mqttAdapter = memo.getMqttAdapter();
        consistManager = memo.getConsistManager();

        this.speedStepMode = SpeedStepMode.NMRA_DCC_128;

        this.isForward = true; //loco should default to forward
        log.debug("MqttThrottle constructor");
    }



    public MqttThrottle(MqttSystemConnectionMemo memo, String sendThrottleTopic, String rcvThrottleTopic,
                    String sendDirectionTopic, String rcvDirectionTopic, String sendFunctionTopic,
                    String rcvFunctionTopic) {
        super(memo);
        mqttAdapter = memo.getMqttAdapter();
        consistManager = memo.getConsistManager();
        this.sendThrottleTopic = sendThrottleTopic;
        this.rcvThrottleTopic = rcvThrottleTopic;
        this.sendDirectionTopic = sendDirectionTopic;
        this.rcvDirectionTopic = rcvDirectionTopic;
        this.sendFunctionTopic = sendFunctionTopic;
        this.rcvFunctionTopic = rcvFunctionTopic;

        this.speedStepMode = SpeedStepMode.NMRA_DCC_128;

        this.isForward = true; //loco should default to forward
        log.debug("MqttThrottle constructor");
    }

    /**
     * Constructor.
     * @param memo system connection.
     * @param address loco address to set on throttle
     */
    public MqttThrottle(MqttSystemConnectionMemo memo, String sendThrottleTopic, String rcvThrottleTopic,
        String sendDirectionTopic, String rcvDirectionTopic, String sendFunctionTopic, String rcvFunctionTopic, LocoAddress address) {
        super(memo);
        mqttAdapter = memo.getMqttAdapter();
        consistManager = memo.getConsistManager();
        this.sendThrottleTopic = sendThrottleTopic;
        this.rcvThrottleTopic = rcvThrottleTopic;
        this.sendDirectionTopic = sendDirectionTopic;
        this.rcvDirectionTopic = rcvDirectionTopic;
        this.sendFunctionTopic = sendFunctionTopic;
        this.rcvFunctionTopic = rcvFunctionTopic;

        this.setDccAddress(address.getNumber());
        this.speedStepMode = SpeedStepMode.NMRA_DCC_128;

        this.isForward = true; //loco should default to forward

        log.debug("MqttThrottle constructor called for address {}", address);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpeedSetting(float speed) {

        super.setSpeedSetting(speed);

        if (speed < 0) {
            speed = 0;
            // Send MQTT message
            jmri.util.ThreadingUtil.runOnLayout(() -> {
                mqttAdapter.publish(this.sendDirectionTopic.replaceFirst("\\$address", String.valueOf(address)), "STOP");
            });
            super.setSpeedSetting(0);
            log.debug("sent address {} direction {}", address, "STOP");
        }

        String intSpeed = String.valueOf(Math.round(speed * 100));

        // Send MQTT message
        jmri.util.ThreadingUtil.runOnLayout(() -> {
            mqttAdapter.publish(this.sendThrottleTopic.replaceFirst("\\$address", String.valueOf(address)), intSpeed);
        });
        log.debug("sent address {} speed {}", address, intSpeed);


    }

    /**
     * Set the direction
     *
     * @param forward true if forward; false otherwise
     */
    @Override
    public void setIsForward(boolean forward) {

        super.setIsForward(forward);
         // Send MQTT message
        jmri.util.ThreadingUtil.runOnLayout(() -> {
            mqttAdapter.publish(this.sendDirectionTopic.replaceFirst("\\$address", String.valueOf(address)), (forward ? "FORWARD" : "REVERSE"));
        });
        log.debug("sent address {} direction {}", address, (forward ? "FORWARD" : "REVERSE"));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendFunctionGroup(int functionNum, boolean momentary) {

        // Send MQTT message
        jmri.util.ThreadingUtil.runOnLayoutEventually(() -> {
            mqttAdapter.publish(this.sendFunctionTopic.replaceFirst("\\$address", String.valueOf(address)).replaceFirst("\\$function",String.valueOf(functionNum)), (getFunction(functionNum) ? "ON" : "OFF"));
        });

        log.debug("sent address {} function {} {}", address, functionNum, (getFunction(functionNum) ? "ON" : "OFF"));

    }


    protected void throttleRelease() {

        active = false;

        // Send blank MQTT message to remove any persistent message
        jmri.util.ThreadingUtil.runOnLayout(() -> {
            mqttAdapter.publish(this.sendThrottleTopic.replaceFirst("\\$address", String.valueOf(address)), "");
            mqttAdapter.publish(this.sendDirectionTopic.replaceFirst("\\$address", String.valueOf(address)), "");

            for (int functionNum = 0; functionNum < getFunctions().length; functionNum++) {
                mqttAdapter.publish(this.sendFunctionTopic.replaceFirst("\\$address",
                    String.valueOf(address)).replaceFirst("\\$function",String.valueOf(functionNum)), "");
            }
        });
        consistManager.deactivateConsist(getLocoAddress());

        mqttAdapter.unsubscribe(this.rcvThrottleTopic.replaceFirst("\\$address", String.valueOf(address)), this);
        mqttAdapter.unsubscribe(this.rcvDirectionTopic.replaceFirst("\\$address", String.valueOf(address)), this);
        mqttAdapter.unsubscribe(this.rcvFunctionTopic.replaceFirst("\\$address", String.valueOf(address)).replaceFirst("\\$function", "#"),  this);

    }

    /**
     * Dispose when finished with this object. After this, further usage of this
     * Throttle object will result in a JmriException.
     *
     * This is quite problematic, because a using object doesn't know when it's
     * the last user.
     */
    @Override
    protected void throttleDispose() {
        log.debug("throttleDispose ", address);

        finishRecord();
    }



    public int setDccAddress(int newaddress) {

        if (address > 0) {
            // Send blank MQTT message to remove any persistent message
            jmri.util.ThreadingUtil.runOnLayout(() -> {
                mqttAdapter.publish(this.sendThrottleTopic.replaceFirst("\\$address", String.valueOf(address)), "");
                mqttAdapter.publish(this.sendDirectionTopic.replaceFirst("\\$address", String.valueOf(address)), "");

                for (int functionNum = 0; functionNum < getFunctions().length; functionNum++) {
                    mqttAdapter.publish(this.sendFunctionTopic.replaceFirst("\\$address",
                        String.valueOf(address)).replaceFirst("\\$function",String.valueOf(functionNum)), "");
                }
            });

            mqttAdapter.unsubscribe(this.rcvThrottleTopic.replaceFirst("\\$address", String.valueOf(address)), this);
            mqttAdapter.unsubscribe(this.rcvDirectionTopic.replaceFirst("\\$address", String.valueOf(address)), this);
            mqttAdapter.unsubscribe(this.rcvFunctionTopic.replaceFirst("\\$address", String.valueOf(address)).replaceFirst("\\$function", "#"), this);
        }
        address = newaddress;

        mqttAdapter.subscribe(this.rcvThrottleTopic.replaceFirst("\\$address", String.valueOf(address)), this);
        mqttAdapter.subscribe(this.rcvDirectionTopic.replaceFirst("\\$address", String.valueOf(address)), this);
        mqttAdapter.subscribe(this.rcvFunctionTopic.replaceFirst("\\$address", String.valueOf(address)).replaceFirst("\\$function", "#"), this);

        consistManager.activateConsist(getLocoAddress());
        setSpeedSetting(0);
        setIsForward(true);

        functionPattern = Pattern.compile(this.rcvFunctionTopic.replaceFirst("\\$address",
            String.valueOf(address)).replaceFirst("\\$function", "(\\\\d+)"));

        return address;
    }

    public int getDccAddress() {
        return address;
    }

    @Override
    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, MqttThrottleManager.isLongAddress(address));
    }

    @Override
    public void notifyMqttMessage(String receivedTopic, String message) {

        if (receivedTopic.endsWith(this.rcvThrottleTopic.replaceFirst("\\$address", String.valueOf(address)))) {
            super.setSpeedSetting(Math.max(0.0f,Math.min(Float.parseFloat(message)/100.0f,1.0f)));

        } else if (receivedTopic.endsWith(this.rcvDirectionTopic.replaceFirst("\\$address",
                    String.valueOf(address)))) {
            switch (message) {
                case "FORWARD":
                    super.setIsForward(true);
                    break;
                case "REVERSE":
                    super.setIsForward(false);
                    break;
                case "STOP":
                    super.setSpeedSetting(-1);
                    break;
                default:
                    log.error("Invalid message {}", message);
            }
        } else {

            Matcher functionMatcher = functionPattern.matcher(receivedTopic);
            if (functionMatcher.matches()) {
                updateFunction(Integer.valueOf(functionMatcher.group(1)),(message.equals("ON")));
            }
        }
    }

    // register for notification
    private final static Logger log = LoggerFactory.getLogger(MqttThrottle.class);




 }
