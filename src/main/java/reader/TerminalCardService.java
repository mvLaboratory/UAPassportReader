package reader;

import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.smartcards.CommandAPDU;
import net.sf.scuba.smartcards.ResponseAPDU;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class TerminalCardService extends CardService {
    private static final long serialVersionUID = 7918176921505623791L;

    private CardTerminal terminal;
 	private Card card;
 	private CardChannel channel;
 	private long lastActiveTime;
 	private int apduCount;

    public TerminalCardService(CardTerminal terminal) {
        this.terminal = terminal;
        lastActiveTime = System.currentTimeMillis();
        apduCount = 0;
    }

    public void open() throws CardServiceException {
        if (isOpen()) { return; }
        try {
            try {
                /* Prefer T=1. */
                card = terminal.connect("T=1");
            } catch (CardException ce) {
                /* If that fails, connect with any protocol available (probably T=0). */
                card = terminal.connect("*");
            }
            channel = card.getBasicChannel();
            if (channel == null) {
                throw new CardServiceException("channel == null");
            }
            state = SESSION_STARTED_STATE;
        } catch (CardException ce) {
            throw new CardServiceException(ce.toString());
        }
    }

    public boolean isOpen() {
        return (state != SESSION_STOPPED_STATE);
    }

    public ResponseAPDU transmit(CommandAPDU ourCommandAPDU) throws CardServiceException {
        try {
            if (channel == null) {
                throw new CardServiceException("channel == null");
            }
            javax.smartcardio.CommandAPDU command = new javax.smartcardio.CommandAPDU(ourCommandAPDU.getBytes());
            javax.smartcardio.ResponseAPDU response = channel.transmit(command);
            ResponseAPDU ourResponseAPDU = new ResponseAPDU(response.getBytes());
            notifyExchangedAPDU(++apduCount, ourCommandAPDU, ourResponseAPDU);
            lastActiveTime = System.currentTimeMillis();
            return ourResponseAPDU;
        } catch (CardException ce) {
            throw new CardServiceException(ce.toString());
        }
    }

    public byte[] getATR() {
        javax.smartcardio.ATR atr = channel.getCard().getATR();
        return atr.getBytes();
    }

    public boolean isExtendedAPDULengthSupported() {
        //		javax.smartcardio.ATR atr = channel.getCard().getATR();
        //		byte[] historicalBytes = atr.getHistoricalBytes();
        return true; // FIXME: check ATR to see if really true
    }

    public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardServiceException {
        try {
            return card.transmitControlCommand(controlCode, command);
        } catch (CardException ce) {
            ce.printStackTrace();
            throw new CardServiceException(ce.toString());
        }
    }

    public void close() {
        try {
            if (card != null) {
                /*
				 * WARNING: Woj: the meaning of the reset flag is actually
				 * reversed w.r.t. to the official documentation, false means
				 * that the card is going to be reset, true means do not reset
				 * This is a bug in the smartcardio implementation from SUN
				 * Moreover, Linux PCSC implementation goes numb if you try to
				 * disconnect a card that is not there anymore.
				 */
                if(terminal.isCardPresent()) {
                    card.disconnect(false);
                }
            }
            state = SESSION_STOPPED_STATE;
        } catch (Exception ce) {
            /* Disconnect failed? Fine... */
        }
    }

    public CardTerminal getTerminal() {
        return terminal;
    }

    /* package visible */ long getLastActiveTime() {
        return lastActiveTime;
    }

    @Override
    public String toString() {
        return "reader.TerminalCardService [" + terminal.getName() + "]";
    }
}
