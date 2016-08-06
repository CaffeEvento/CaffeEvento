package impl.events;

import api.events.Event;
import api.events.EventHandler;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * This has to be abstract in order to allow for transparent serialization over the network.
 * Created by chris on 7/1/16.
 */
public final class EventHandlerImpl implements EventHandler {
    private EventHandlerData eventHandlerData = new EventHandlerData();
    private List<Consumer<Event>> eventConsumers = new ArrayList<>();
    private Log log = LogFactory.getLog(getClass());

    private EventHandlerImpl() {
    } // Haha, nobody can create me now!

    private EventHandlerImpl(EventHandlerImpl eventHandler) {
        this.eventHandlerData = new EventHandlerData(eventHandler.eventHandlerData);
        this.eventConsumers = new ArrayList<>(eventHandler.eventConsumers);
    }

    @Override
    public final UUID getEventHandlerId() {
        return eventHandlerData.getEventHandlerId();
    }

    @Override
    public String encodeToJson() {
        return new GsonBuilder().create().toJson(eventHandlerData);
    }

    @Override
    public void addIpDestination(String url) {
        eventHandlerData.httpEventReceiver = url;
    }

    @Override
    public EventHandler getCopy() {
        return new EventHandlerImpl(this);
    }

    @Override
    public void handleEvent(Event theEvent) {
        if (eventConsumers.size() == 0) {
            log.info("No event consumers registered for event handler with id: " + eventHandlerData.getEventHandlerId());
        }
        eventConsumers.forEach(c -> c.accept(theEvent));
    }

    @Override
    public Predicate<Event> getHandlerCondition() {
        List<Predicate<Event>> predicates = new ArrayList<>();

        Optional.ofNullable(eventHandlerData.eventName)
                .map(name ->
                        (Predicate<Event>) event ->
                                Optional.ofNullable(event.getEventName())
                                        .map(n -> n.equals(name)).orElse(false))
                .ifPresent(predicates::add);
        Optional.ofNullable(eventHandlerData.eventType)
                .map(type ->
                        (Predicate<Event>) event ->
                                Optional.ofNullable(event.getEventType())
                                        .map(n -> n.equals(type)).orElse(false))
                .ifPresent(predicates::add);
        for(Map.Entry<String, String> expectedEntry : eventHandlerData.eventData.entrySet()) {
            String eKey = expectedEntry.getKey();
            String eValue = expectedEntry.getValue();
            Predicate<Event> predicate = event -> Optional.ofNullable(event.getEventField(eKey))
                    .map(v -> v.equals(eValue)).orElse(false);
            predicates.add(predicate);
        }

        for(String expectedKey : eventHandlerData.hasKeys) {
            Predicate<Event> predicate = event -> Optional.ofNullable(event.getEventField(expectedKey)).isPresent();
            predicates.add(predicate);
        }

        for(Map.Entry<String, String> expectedLikeEntry : eventHandlerData.eventDataLike.entrySet()) {
            String eKey = expectedLikeEntry.getKey();
            String eValue = expectedLikeEntry.getValue();
            Predicate<Event> predicate = event -> Optional.ofNullable(event.getEventField(eKey))
                    .map(field -> Pattern.matches(eValue, field)).orElse(false);
            predicates.add(predicate);
        }

        return predicates.stream().reduce(event -> true, Predicate::and);
    }

    private void sendHttpEvent(URI destination, Event theEvent) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(destination);
        try {
            post.addHeader("content-type", "text/json");
            post.setEntity(new StringEntity(theEvent.encodeEvent()));
            client.execute(post);
        } catch (IOException e) {
            log.error("There was an error sending the event.", e);
        }
    }

    public static Optional<EventHandler> fromJson(String json) {
        try {
            return Optional.of(decodeFromEventData(new GsonBuilder().create().fromJson(json, EventHandlerData.class)));
        }catch(JsonSyntaxException e){
            return Optional.empty();
        }
    }

    public static EventHandlerBuilder create() {
        return new EventHandlerBuilder();
    }

    private static EventHandler decodeFromEventData(EventHandlerData theEventHandlerData) {
        EventHandlerImpl handler = new EventHandlerImpl();
        Optional.ofNullable(theEventHandlerData.httpEventReceiver).map(URI::create)
                .ifPresent(uri -> handler.addEventConsumer(event -> handler.sendHttpEvent(uri, event)));
        handler.eventHandlerData = theEventHandlerData;
        return handler;
    }

    private void addEventConsumer(Consumer<Event> eventConsumer) {
        this.eventConsumers.add(eventConsumer);
    }

    private static class EventHandlerData {
        private UUID eventHandlerId = UUID.randomUUID();
        private String eventName;
        private String eventType;
        private String httpEventReceiver;
        private String socketEventReceiver;
        private Map<String, String> eventData = new HashMap<>();
        private Map<String, String> eventDataLike = new HashMap<>();
        private Set<String> hasKeys = new HashSet<>();

        private EventHandlerData() {
        }

        private EventHandlerData(EventHandlerData data) {
            this.eventHandlerId = data.eventHandlerId;
            this.eventName = data.eventName;
            this.eventType = data.eventType;
            this.httpEventReceiver = data.httpEventReceiver;
            this.socketEventReceiver = data.socketEventReceiver;
            this.eventData = new HashMap<>(data.eventData);
            this.eventDataLike = new HashMap<>(data.eventDataLike);
            this.hasKeys = new HashSet<>(hasKeys);
        }

        UUID getEventHandlerId() {
            return eventHandlerId;
        }

        void eventName(String eventName) {
            this.eventName = eventName;
        }

        void eventType(String eventType) {
            this.eventType = eventType;
        }

        void httpEventReceiver(String url) {
            this.httpEventReceiver = url;
        }

        void eventData(String key, String value) {
            this.eventData.put(key, value);
        }

        void eventDataLike(String key, String value) {
            this.eventDataLike.put(key, value);
        }

        void hasKeys(String key) {
            this.hasKeys.add(key);
        }
    }

    /**
     * Created by chris on 7/11/16.
     */
    public static class EventHandlerBuilder {
        private EventHandlerData eventHandlerData = new EventHandlerData();
        private List<Consumer<Event>> eventConsumers = new ArrayList<>();

        private EventHandlerBuilder() {
        }

        /**
         * Creates and returns a copy of the event handler builder without the handlers
         * @return a copy of this EventHandlerBuilder containing only the criterion created by this builder
         */
        public EventHandlerBuilder cloneOnlyCriteria() {
            EventHandlerBuilder clone = EventHandler.create();
            clone.eventHandlerData = new EventHandlerData(this.eventHandlerData);
            return clone;
        }

        /**
         * Builds the event handler builder with the event data
         * @return EventHandler containing data created by this builder
         */
        public EventHandler build() {
            EventHandlerImpl handler = new EventHandlerImpl();
            handler.eventHandlerData = eventHandlerData;
            eventConsumers.forEach(handler::addEventConsumer);
            return handler;
        }

        /**
         * Match for the event name
         * @param name the event name to match
         * @return EventHandlerBuilder
         */
        public EventHandlerBuilder eventName(String name) {
            eventHandlerData.eventName(name);
            return this;
        }

        /**
         * Match for the event type
         * @param type the event type to match
         * @return EventHandlerBuilder
         */
        public EventHandlerBuilder eventType(String type) {
            eventHandlerData.eventType(type);
            return this;
        }

        /**
         * The http endpoint where the event should be received
         * @param httpEventReceiver the http endpoint to send the events to
         * @return EventHandlerBuilder
         */
        public EventHandlerBuilder httpEventReceiver(String httpEventReceiver) {
            eventHandlerData.httpEventReceiver(httpEventReceiver);
            return this;
        }

        /**
         * Event data at key is matched with the value
         * @param key key to look for value at
         * @param value value to match
         * @return EventHandlerBuilder
         */
        public EventHandlerBuilder eventData(String key, String value) {
            eventHandlerData.eventData(key, value);
            return this;
        }

        /**
         * Key is the key to check, value is a regular expression attempting to match that key.
         * If the key does not exist, then the event handler will not handle the event.
         * @param key key to look for value at
         * @param value regular expression to use for matching
         * @return EventHandlerBuilder
         */
        public EventHandlerBuilder eventDataLike(String key, String value) {
            eventHandlerData.eventDataLike(key, value);
            return this;
        }

        /**
         * Checks if the event has the data key
         * @param key the data key to check for
         * @return EventHanderBuilder
         */
        public EventHandlerBuilder hasDataKey(String key) {
            eventHandlerData.hasKeys(key);
            return this;
        }

        /**
         * Adds a handler action to the event handler, this is fired when the conditions of the event
         * handler are met
         * @param consumer what to do with the event and its data once it is received
         * @return EventHandlerBuilder
         */
        public EventHandlerBuilder eventHandler(Consumer<Event> consumer) {
            eventConsumers.add(consumer);
            return this;
        }
    }
}
