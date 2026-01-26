package com.example.refinitivtreasury.service;

import com.example.refinitivtreasury.dto.TreasuryPriceDto;
import com.refinitiv.ema.access.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class RefinitivOmmConsumer implements OmmConsumerClient {

    private static final Logger logger = LoggerFactory.getLogger(RefinitivOmmConsumer.class);

    private OmmConsumer consumer;
    private final ConcurrentLinkedQueue<TreasuryPriceDto> messageQueue = new ConcurrentLinkedQueue<>();
    private final List<Long> itemHandles = new ArrayList<>();

    public void initialize() {
        try {
            OmmConsumerConfig config = EmaFactory.createOmmConsumerConfig()
                    .host("your-refinitiv-host:14002") // Replace with your Refinitiv host
                    .username("your-username")         // Replace with your username
                    .password("your-password");        // Replace with your password
            consumer = EmaFactory.createOmmConsumer(config);
            logger.info("OmmConsumer initialized successfully");
        } catch (OmmException e) {
            logger.error("Failed to initialize OmmConsumer", e);
            throw new RuntimeException("Failed to initialize OmmConsumer", e);
        }
    }

    public void subscribeToChain(String chainRic) {
        try {
            long handle = consumer.registerClient(
                    EmaFactory.createReqMsg().serviceName("ELEKTRON_DD").name(chainRic), this);
            itemHandles.add(handle);
            logger.info("Subscribed to chain RIC: {}", chainRic);
        } catch (OmmException e) {
            logger.error("Failed to subscribe to chain RIC: {}", chainRic, e);
        }
    }

    @Override
    public void onRefreshMsg(RefreshMsg refreshMsg, OmmConsumerEvent event) {
        logger.debug("Received refresh message for RIC: {}", refreshMsg.name());
        processMessage(refreshMsg, "REFRESH");
    }

    @Override
    public void onUpdateMsg(UpdateMsg updateMsg, OmmConsumerEvent event) {
        logger.debug("Received update message for RIC: {}", updateMsg.name());
        processMessage(updateMsg, "UPDATE");
    }

    @Override
    public void onStatusMsg(StatusMsg statusMsg, OmmConsumerEvent event) {
        logger.info("Received status message for RIC: {}, State: {}", 
                    statusMsg.name(), statusMsg.state());
    }

    private void unsubscribeAll() {
        for (Long handle : itemHandles) {
            try {
                consumer.unregister(handle);
                logger.info("Unsubscribed handle: {}", handle);
            } catch (OmmException e) {
                logger.error("Failed to unsubscribe handle: {}", handle, e);
            }
        }
        itemHandles.clear();
    }

    private void shutdown() {
        if (consumer != null) {
            try {
                consumer.uninitialize();
                logger.info("OmmConsumer shutdown successfully");
            } catch (OmmException e) {
                logger.error("Failed to shutdown OmmConsumer", e);
            }
        }
    }

    private void processMessage(Msg msg, String updateType) {
        try {
            if (msg.domainType() == EmaRdm.MMT_MARKET_PRICE) {
                processMarketPrice(msg, updateType);
            } else if (msg.domainType() == EmaRdm.MMT_MARKET_BY_PRICE) {
                processChain(msg);
            }
        } catch (Exception e) {
            logger.error("Error processing message for RIC: {}", msg.name(), e);
        }
    }

    private void processMarketPrice(Msg msg, String updateType) {
        TreasuryPriceDto dto = new TreasuryPriceDto();
        dto.setRic(msg.name());
        dto.setTimestamp(LocalDateTime.now());
        dto.setUpdateType(updateType);
        dto.setLastUpdate(LocalDateTime.now());

        FieldList fieldList = msg.payload().fieldList();
        for (FieldEntry entry : fieldList) {
            // Verify field IDs with your Refinitiv data dictionary
            switch (entry.fieldId()) {
                case 22: // BID
                    dto.setBidPrice(entry.doubleValue());
                    break;
                case 25: // ASK
                    dto.setAskPrice(entry.doubleValue());
                    break;
                case 393: // YLD_1 (Bid Yield)
                    dto.setBidYield(entry.doubleValue());
                    break;
                case 396: // ASK_YLD
                    dto.setAskYield(entry.doubleValue());
                    break;
            }
        }

        // Validate data before queuing
        if (dto.getRic() != null && !dto.getRic().isEmpty()) {
            messageQueue.add(dto);
            logger.debug("Queued TreasuryPriceDto for RIC: {}", dto.getRic());
        } else {
            logger.warn("Invalid RIC in message: {}", msg.name());
        }
    }

    public void processChain(Msg msg) {
        if (msg.payload().map() != null) {
            Map map = msg.payload().map();
            for (MapEntry entry : map) {
                if (entry.key().dataType() == DataType.DataTypes.ASCII) {
                    String ric = entry.key().ascii();
                    try {
                        long handle = consumer.registerClient(
                                EmaFactory.createReqMsg().serviceName("ELEKTRON_DD").name(ric), this);
                        itemHandles.add(handle);
                        logger.info("Subscribed to chain constituent RIC: {}", ric);
                    } catch (OmmException e) {
                        logger.error("Failed to subscribe to RIC: {}", ric, e);
                    }
                }
            }
        }
    }

    public TreasuryPriceDto pollMessage() {
        return messageQueue.poll();
    }
}