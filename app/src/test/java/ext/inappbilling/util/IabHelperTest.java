package ext.inappbilling.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.test.suitebuilder.annotation.SmallTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Tests for workarounds added to {@link IabHelper}
 *
 * Created by jakub on 18/04/16.
 */
public class IabHelperTest {

  @Mock Context context;

  IabHelper iabHelper;

  @Before
  public void setup() {
    initMocks(this);
    when(context.getApplicationContext()).thenReturn(context);
    iabHelper = new IabHelper(context, "DUMMY_KEY");
  }

  /**
   * Method {@link IabHelper#resumePurchase(int, IabHelper.OnIabPurchaseFinishedListener)} was added
   * to enable resuming purchase flow after activity is recreated (e.g. orientation change).
   * <p/>
   * It is expected to set IabHelper state in way that enables reporting valid Purchase even after
   * instance recreation.
   *
   * @see http://stackoverflow.com/questions/18223130/in-app-billing-rapid-device-orientation-causes-crash-illegalstateexception
   */
  @Test
  public void resumePurchase_reportsValidPurchase() {
    // GIVEN:
    int requestCode = 1232134;
    IabHelper.OnIabPurchaseFinishedListener listener = mock(IabHelper.OnIabPurchaseFinishedListener.class);

    // ON:
    iabHelper.resumePurchase(requestCode, listener);

    // IT: sets values so purchase is reported event after instance recreation
    assertEquals(requestCode, iabHelper.mRequestCode);
    assertEquals(listener, iabHelper.mPurchaseListener);
  }

  /**
   * There is an issue in {@link IabHelper} implementation provided by google. If Play Store is stopped
   * when IabHelper is already initialised, service disconnect occurs but is not reported anywhere,
   * resulting in unexpected IabHelper behaviour.
   * <p/>
   * There is a fix expected in {@link IabHelper#startSetup(IabHelper.OnIabSetupFinishedListener)}, that reports
   * service disconnect via listener.
   */
  @SuppressWarnings("WrongConstant")
  @Test
  public void startSetup_reportsServiceDisconnected() {
    // GIVEN: context reports service exists
    PackageManager pm = mock(PackageManager.class);
    when(pm.queryIntentServices(any(Intent.class), anyInt())).thenReturn(singletonList(new ResolveInfo()));
    when(context.getPackageManager()).thenReturn(pm);

    // GIVEN: bind service reports onServiceDisconnected immidiately
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ServiceConnection serviceConnection = (ServiceConnection) invocation.getArguments()[1];
        serviceConnection.onServiceDisconnected(new ComponentName("dummy", "dummy"));
        return null;
      }
    }).when(context).bindService(any(Intent.class), any(ServiceConnection.class), anyInt());

    // GIVEN: mock listener
    IabHelper.OnIabSetupFinishedListener listener = mock(IabHelper.OnIabSetupFinishedListener.class);

    // ON: startSetup() call
    iabHelper.startSetup(listener);

    // IT: reports service disconnected
    ArgumentCaptor<IabResult> resultCaptor = ArgumentCaptor.forClass(IabResult.class);
    verify(listener).onIabSetupFinished(resultCaptor.capture());
    assertFalse(resultCaptor.getValue().isSuccess());
    assertEquals(IabHelper.BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE, resultCaptor.getValue().getResponse());
  }
}
