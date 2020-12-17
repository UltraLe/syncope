package org.apache.syncope.common.rest.api.batch;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(value= Suite.class)
@Suite.SuiteClasses(value={BatchPayloadLineReaderTest.class, BatchPayloadParserTest.class, BatchPayloadParsetTestImproved.class, BatchPayloadLineReaderTestImproved.class})
public class BatchPayloadTestSuite {
}
