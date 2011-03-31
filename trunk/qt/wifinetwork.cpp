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
    if (  ssid.startsWith("DLink-") && ( ssid.count(QRegExp("[a-fA-F0-9]{6}")) == 1) )
    {
            ssidSubpart = ssid.right(6);
            type = DLINK;
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
    if ( ssid.count(QRegExp("InfostradaWiFi-[0-9a-zA-Z]{6}")) ==   1 )
    {
            ssidSubpart = ssid;
            type = INFOSTRADA;
            return true;
    }
    return false;
}
