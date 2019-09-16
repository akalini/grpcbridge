package grpcbridge.http;

import com.google.api.HttpRule;
import org.junit.Test;

public class BridgeHttpRuleTest {
    @Test(expected = UnsupportedOperationException.class)
    public void createWithUndefinedRule() {
        HttpRule rule = HttpRule.getDefaultInstance();
        BridgeHttpRule.create(rule);
    }
}
