#include "wlan6keygen.h"

Wlan6Keygen::Wlan6Keygen( WifiNetwork * router ) : KeygenThread(router) {}

void Wlan6Keygen::run(){
    if ( router == NULL )
            return;
    if ( router->getMac() == "" )
    {
            //TODO
            return;
    }
    QString ssidStr = router->getSSIDsubpart();
    QString macStr = router->getMac().right(2);
    char ssidSubPart[] = {'1', '2','3', '4', '5','6' };/*These values are not revelant.*/
    char bssidLastByte[] = { '6', '6' };
    ssidSubPart[0] = ssidStr.at(0).toAscii();
    ssidSubPart[1] = ssidStr.at(1).toAscii();
    ssidSubPart[2] = ssidStr.at(2).toAscii();
    ssidSubPart[3] = ssidStr.at(3).toAscii();
    ssidSubPart[4] = ssidStr.at(4).toAscii();
    ssidSubPart[5] = ssidStr.at(5).toAscii();
    bssidLastByte[0] = macStr.at(0).toAscii();
    bssidLastByte[1] = macStr.at(1).toAscii();
    for ( int  k = 0; k < 6 ; ++k )
        if( ssidSubPart[k] >= 'A')
            ssidSubPart[k] = (char)(ssidSubPart[k] - 55);

    if(bssidLastByte[0] >= 'A' )
        bssidLastByte[0] = (char)(bssidLastByte[0] - 55);
    if(bssidLastByte[1] >= 'A' )
        bssidLastByte[1] = (char)(bssidLastByte[1] - 55);

    int bytes[13];
    for ( int i = 0; i < 10 ; ++i )
    {
            /*Do not change the order of this instructions*/
            int aux = i + ( ssidSubPart[3] & 0xf ) +  ( bssidLastByte[0] & 0xf ) + ( bssidLastByte[1] & 0xf );
            int aux1 = ( ssidSubPart[1] & 0xf ) + ( ssidSubPart[2] & 0xf ) + ( ssidSubPart[4] & 0xf ) + ( ssidSubPart[5] & 0xf );
            bytes[1] = aux ^ ( ssidSubPart[5] & 0xf );
            bytes[5] = aux ^ ( ssidSubPart[4] & 0xf );
            bytes[9] = aux ^ ( ssidSubPart[3] & 0xf );
            bytes[2] = aux1 ^ ( ssidSubPart[2] & 0xf );
            bytes[6] = aux1 ^  ( bssidLastByte[0] & 0xf );
            bytes[10] = aux1 ^ ( bssidLastByte[1] & 0xf );
            bytes[3] =  ( bssidLastByte[0] & 0xf ) ^ ( ssidSubPart[5] & 0xf );
            bytes[7] = ( bssidLastByte[1] & 0xf ) ^ ( ssidSubPart[4] & 0xf );
            bytes[11] = aux ^ aux1;
            bytes[4] = bytes[1] ^ bytes[7];
            bytes[8] = bytes[6] ^ bytes[10];
            bytes[12] = bytes[2] ^ bytes[9];
            bytes[0] = bytes[11] ^ bytes[5];
            QString key = "", tmp = "";
            for ( int j = 0 ; j < 13 ; ++j )
            {
                tmp.setNum(bytes[j] & 0xf ,16);
                key += tmp;
            }
            results.append(key.toUpper());
    }
    //TODO:consistency check
    return;
}
