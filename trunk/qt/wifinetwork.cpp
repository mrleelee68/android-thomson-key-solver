#include "wifinetwork.h"
#include <QRegExp>
WifiNetwork::WifiNetwork(QString r , QString m) : ssid(r) , mac(m)
{
    newThomson = false;/*Must be first*/
    supported = ssidFilter();
}

QString WifiNetwork::getSSID() const {
    return this->ssid;
}
QString WifiNetwork::getMac() const{
    QString mac = this->mac;
    return mac.replace(QChar(':') , "" );
}

QString WifiNetwork::getSSIDsubpart() const{
    return this->ssidSubpart;
}

int  WifiNetwork::getType() const{
    return this->type;
}

bool WifiNetwork::isSupported() const{
    return this->supported && !this->newThomson;
}

bool WifiNetwork::ssidFilter(){
    if ( ( ssid.count(QRegExp("[a-fA-F0-9]{6}")) == 1) && (
             ssid.startsWith("Thomson") || ssid.startsWith("SpeedTouch") ||
             ssid.startsWith("O2Wireless") || ssid.startsWith("Orange-") ||
             ssid.startsWith("INFINITUM") || ssid.startsWith("BigPond")  ||
             ssid.startsWith("Otenet") || ssid.startsWith("Bbox-") ||
             ssid.startsWith("DMAX") || ssid.startsWith("privat") ||
             ssid.startsWith("TN_private_") || ssid.startsWith("Cyta") ) )
    {
            ssidSubpart = ssid.right(6);
            if ( !mac.isEmpty() )
                    if ( ssidSubpart == getMac().right(6) )
                            newThomson = true;
            type = THOMSON;
            return true;
    }
    if (  ( ssid.count(QRegExp("DLink-[a-fA-F0-9]{6}")) == 1) && ( ssid.size() == 12 ) )
    {
            ssidSubpart = ssid.right(6);
            type = DLINK;
            return true;
    }
    if ( ssid.count(QRegExp("Discus--?[0-9a-fA-F]{6}")) == 1)
    {
            ssidSubpart = ssid.right(6);
            type = DISCUS;
            return true;
    }
    if ( (ssid.count(QRegExp("eircom[0-7]{4} [0-7]{4}")) == 1 )||
         (ssid.count(QRegExp("eircom[0-7]{8}")) == 1))
    {
        if (  ssid.size() == 14 )
            ssidSubpart = ssid.right(8);
        else
            ssidSubpart = ssid.mid(6,4) + ssid.right(4);
            if ( mac.isEmpty() )
                    calcEircomMAC();
            type = EIRCOM;
        return true;
    }
    if ( ssid.size() == 5  &&
          ( mac.startsWith("00:1F:90") || mac.startsWith("A8:39:44") ||
            mac.startsWith("00:18:01") || mac.startsWith("00:20:E0") ||
            mac.startsWith("00:0F:B3") || mac.startsWith("00:1E:A7") ||
            mac.startsWith("00:15:05") || mac.startsWith("00:24:7B") ||
            mac.startsWith("00:26:62") || mac.startsWith("00:26:B8") ) )
    {
            ssidSubpart = ssid;
            type = VERIZON;
            return true;
    }
    if ( ssid.count(QRegExp("TECOM-AH4021-[0-9a-zA-Z]{6}|TECOM-AH4222-[0-9a-zA-Z]{6}")) == 1 )
    {
            ssidSubpart = ssid;
            type = TECOM;
            return true;
    }
    if ( ( ssid.count(QRegExp("SKY[0-9]{5}"))==1) && (mac.startsWith("C4:3D:C7") ||
          mac.startsWith("E0:46:9A") ||  mac.startsWith("E0:91:F5") ||
          mac.startsWith("00:09:5B") ||  mac.startsWith("00:0F:B5") ||
          mac.startsWith("00:14:6C") ||  mac.startsWith("00:18:4D") ||
          mac.startsWith("00:26:F2") ||  mac.startsWith("C0:3F:0E") ||
          mac.startsWith("30:46:9A") ||  mac.startsWith("00:1B:2F") ||
          mac.startsWith("A0:21:B7") ||  mac.startsWith("00:1E:2A") ||
          mac.startsWith("00:1F:33") ||  mac.startsWith("00:22:3F") ||
          mac.startsWith("00:24:B2") ) )
    {
            ssidSubpart = ssid.right(5);
            type = SKY_V1;
            return true;
    }
    if ( ssid.count(QRegExp("InfostradaWiFi-[0-9a-zA-Z]{6}")) ==   1 )
    {
            ssidSubpart = ssid;
            type = INFOSTRADA;
            return true;
    }
    if ( ( ssid.count(QRegExp("WLAN_[0-9a-zA-Z]{4}|JAZZTEL_[0-9a-zA-Z]{4}")) == 1 ) &&
        ( mac.startsWith("00:1F:A4") || mac.startsWith("64:68:0C") ||
              mac.startsWith("00:1D:20") ) )
    {
            ssidSubpart = ssid.right(4);
            type = WLAN4;
            return true;
    }
    return false;
}
void WifiNetwork::calcEircomMAC(){
    QString end;
    bool status = false;
    int ssidNum = ssidSubpart.toInt(&status , 8 ) ^ 0x000fcc;
    end.setNum(ssidNum,16);
    while ( end.size() < 6 )
        end = "0" + end;
    end = end.toUpper();
    this->mac = "00:0F:CC:" + end.left(2)+ ":" +
                           end.mid(2,2)+ ":" + end.right(2);
}
