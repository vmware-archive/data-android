package io.pivotal.android.data;

import android.content.Context;
import android.test.AndroidTestCase;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public class RemoteStoreTest extends AndroidTestCase {

    public static final String TEST_TOKEN = "TOKEN";
    public static final String TEST_BASE_URL = "http://www.test.com";
    public static final String TEST_COLLECTION = "objects";
    public static final String TEST_KEY = "key";
    public static final String TEST_VALUE = "value";

    public void testGetWithRemoteStore() {
        final RemoteStore dataStore = new TestRemoteStore(TEST_COLLECTION);
        final DataObject object = new DataObject(dataStore, TEST_KEY);
        object.addObserver(new DataObject.Observer() {
            @Override
            public void onChange(final String key, final String value) {
                fail();
            }

            @Override
            public void onError(final String key, final Error error) {
                fail();
            }
        });

        assertEquals(TEST_VALUE, object.get(TEST_TOKEN));
    }

    public void testPutWithRemoteStore() {
        final AssertionLatch latch = new AssertionLatch(1);
        final RemoteStore dataStore = new TestRemoteStore(TEST_COLLECTION);
        final DataObject object = new DataObject(dataStore, TEST_KEY);
        object.addObserver(new DataObject.Observer() {
            @Override
            public void onChange(final String key, final String value) {
                latch.countDown();
                assertEquals("value1", value);
            }

            @Override
            public void onError(final String key, final Error error) {
                fail();
            }
        });

        object.put(TEST_TOKEN, "value1");
        latch.assertComplete();
    }

    private final class TestRemoteStore extends RemoteStore {

        public TestRemoteStore(final String collection) {
            super(null, collection);
        }

        @Override
        /* package */ ObserverHandler createObserverHandler(final Set<Observer> observers, final Object lock) {
            return new FakeObserverHandler(observers, lock);
        }

        @Override
        /* package */ RemoteClient createRemoteClient(final Context context) {
            return new FakeRemoteClient(getUrl(), TEST_VALUE);
        }

        private URI getUrl() {
            try {
                return new URI(TEST_BASE_URL + "/" + TEST_COLLECTION + "/" + TEST_KEY);
            } catch (URISyntaxException e) {
                return null;
            }
        }
    }
}
