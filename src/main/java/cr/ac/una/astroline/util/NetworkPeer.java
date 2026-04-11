package cr.ac.una.astroline.util;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.List;

/**
 * Descubrimiento de peers en la red local via UDP broadcast.
 * Cada instancia anuncia su presencia y responde a otros peers.
 *
 * @author JohanDanilo
 */
public class NetworkPeer {

    private static final int    DISCOVERY_PORT = 9090;
    private static final String DISCOVER_MSG   = "ASTROLINE_DISCOVER";
    private static final String RESPONSE_MSG   = "ASTROLINE_HERE";
    private static final int    TIMEOUT_MS     = 2000;

    private static Thread  listenerThread;
    private static volatile boolean running = false;

    private NetworkPeer() {}

    /**
     * Inicia el listener UDP en background.
     * Cuando recibe un DISCOVER, responde con ASTROLINE_HERE.
     */
    public static void startListening() {
        running = true;
        listenerThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
                byte[] buf = new byte[256];
                while (running) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength()).trim();
                    if (DISCOVER_MSG.equals(msg)) {
                        byte[] response = RESPONSE_MSG.getBytes();
                        DatagramPacket reply = new DatagramPacket(
                            response, response.length,
                            packet.getAddress(), packet.getPort()
                        );
                        socket.send(reply);
                    }
                }
            } catch (IOException e) {
                if (running) System.err.println("[NetworkPeer] Error en listener: " + e.getMessage());
            }
        }, "astroline-discovery-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Envía broadcast UDP y recolecta IPs que respondan.
     * Filtra la propia IP para no tratarse a sí mismo como peer.
     *
     * @return lista de IPs de otros peers encontrados
     */
    
    public static List<String> discoverPeers() {
        List<String> peers = new ArrayList<>();
        String ownIp = getOwnIp();

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MS);

            // ✅ Calcular broadcast dirigido a la subred local
            String broadcastIp = getBroadcastAddress(ownIp);
            byte[] msg = DISCOVER_MSG.getBytes();
            DatagramPacket packet = new DatagramPacket(
                msg, msg.length,
                InetAddress.getByName(broadcastIp), DISCOVERY_PORT
            );
            socket.send(packet);

            byte[] buf = new byte[256];
            long deadline = System.currentTimeMillis() + TIMEOUT_MS;
            while (System.currentTimeMillis() < deadline) {
                try {
                    DatagramPacket response = new DatagramPacket(buf, buf.length);
                    socket.receive(response);
                    String responseMsg = new String(response.getData(), 0, response.getLength()).trim();
                    if (RESPONSE_MSG.equals(responseMsg)) {
                        String ip = response.getAddress().getHostAddress();
                        if (!ip.equals(ownIp) && !peers.contains(ip)) {
                            peers.add(ip);
                            System.out.println("[NetworkPeer] Peer encontrado: " + ip);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[NetworkPeer] Error en discovery: " + e.getMessage());
        }
        return peers;
    }

    public static String getOwnIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                // Ignorar loopback, virtuales y desconectados
                if (ni.isLoopback() || !ni.isUp() || ni.isVirtual()) continue;
                // Ignorar adaptadores de VirtualBox/VMware por nombre
                String nombre = ni.getName().toLowerCase() + ni.getDisplayName().toLowerCase();
                if (nombre.contains("virtual") || nombre.contains("vmware") || 
                    nombre.contains("vbox")    || nombre.contains("loopback")) continue;

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("[NetworkPeer] Error obteniendo IP: " + e.getMessage());
        }
        return "127.0.0.1";
    }
    
    private static String getBroadcastAddress(String ip) {
        // Asume /24 — suficiente para red doméstica o de oficina
        String[] parts = ip.split("\\.");
        return parts[0] + "." + parts[1] + "." + parts[2] + ".255";
    }

    public static void stop() {
        running = false;
        if (listenerThread != null) listenerThread.interrupt();
    }
}