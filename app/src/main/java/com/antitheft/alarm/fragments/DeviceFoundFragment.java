package com.antitheft.alarm.fragments;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.antitheft.alarm.R;
import com.antitheft.alarm.adapter.BluetoothDevicesAdapter;
import com.antitheft.alarm.model.LibraState;
import com.antitheft.alarm.utils.Const;
import com.inuker.bluetooth.library.search.SearchResult;
import java.util.List;
import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceFoundFragment extends BaseFragment {

    @BindView(R.id.btdevices)
    RecyclerView btDevicelistView;

    private static DeviceFoundFragment instance;
    private BluetoothDevicesAdapter devicesAdapter;
    private List<SearchResult> deviceList;

    public DeviceFoundFragment() {
        // Required empty public constructor
    }

    public static DeviceFoundFragment newInstance() {
        return new DeviceFoundFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_btdevices;
    }

    @Override
    public void initData() {
        deviceList = (List<SearchResult>) arg;
        if (deviceList != null && deviceList.size() > 0) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            btDevicelistView.setLayoutManager(layoutManager);
            devicesAdapter = new BluetoothDevicesAdapter(getContext(), deviceList);
            btDevicelistView.setAdapter(devicesAdapter);
            devicesAdapter.notifyDataSetChanged();
            devicesAdapter.setOnItemViewClickListener(new BluetoothDevicesAdapter.OnItemViewClickListener() {
                @Override
                public void onItemClick(int position) {
                    //FragmentStack.getInstance().clear();
                    bleDisconnect(LibraState.getInstance().getMac());
                    LibraState.getInstance().setMac(deviceList.get(position).getAddress());
                    goTo(Const.LIBRA_CONNECTED_STATE_ID);
                }
            });
        } else {
            goTo(Const.DEVICE_PAIRING_ID);
        }
    }

    @Override
    public void initViewEvent() {

    }

    @Override
    public int getFragmentId() {
        return Const.DEVICE_FOUND_ID;
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
