package t.inmethod.example;


import android.view.View;
import android.widget.TextView;


import java.math.BigInteger;
import java.util.List;

import inmethod.android.bt.BTInfo;
import inmethod.android.bt.ScanRecord;
import inmethod.commons.util.HexAndStringConverter;
import t.inmethod.viewdesign.R;

public class DeviceInfo extends BTInfo{


    private DeviceInfo(){}

    public DeviceInfo(BTInfo aBTInfo){
        setDeviceName(aBTInfo.getDeviceName());
        setDeviceAddress(aBTInfo.getDeviceAddress());
        setAdvertisementData(aBTInfo.getAdvertisementData());
        setDeviceBlueToothType(aBTInfo.getDeviceBlueToothType());
        setRSSI(aBTInfo.getRSSI());
    }

    public static void mapDeviceInfoToLayout(Object[] layoutData, Object aDeviceObject ){
         DeviceInfo aDeviceInfo = (DeviceInfo)aDeviceObject;
        ((TextView)layoutData[0]).setText( aDeviceInfo.getDeviceName());
        ((TextView)layoutData[1]).setText(aDeviceInfo.getDeviceAddress());
        String sAdvertisement = "";

        List<ScanRecord> aScanRecord = ScanRecord.parseScanRecord(aDeviceInfo.getAdvertisementData());
        for (ScanRecord record : aScanRecord) {
            if (record.getType() == (byte) 0xff)
                sAdvertisement = sAdvertisement + "\n" + "Type=0x"
                        + HexAndStringConverter.convertHexByteToHexString(record.getType()) + ",Data=ASC("
                        + new String(record.getData()).trim() + ") (HEX:"
                        + HexAndStringConverter.convertHexByteToHexString(record.getData()) + "),DEC(" + (new BigInteger(1, record.getData())).longValue() + ")";
            else
                sAdvertisement = sAdvertisement + "\n" + "Type=0x"
                        + HexAndStringConverter.convertHexByteToHexString(record.getType()) + ",Data=ASC("
                        + new String(record.getData()).trim() + ") (HEX:"
                        + HexAndStringConverter.convertHexByteToHexString(record.getData()) + ")";

        }
        ((TextView)layoutData[2]).setText(sAdvertisement);

    }

    public static Object[] getDeviceInfoFromLayoutId(View view){
        TextView[] aLayoutData = new TextView[3];
        aLayoutData[0] = (TextView) view.findViewById(R.id.device_name);
        aLayoutData[1] = (TextView) view.findViewById(R.id.device_address);
        aLayoutData[2] =  (TextView) view.findViewById(R.id.device_advertisement);
        return aLayoutData;
    }
}
