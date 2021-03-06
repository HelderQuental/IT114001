import client.ClientUI;
import client.SocketClient;
import client.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

public class Room implements AutoCloseable {
    private static SocketServer server;// used to refer to accessible server functions
    private String name;
    private final static Logger log = Logger.getLogger(Room.class.getName());

    // Commands
    private final static String COMMAND_TRIGGER = "/";
    private final static String CREATE_ROOM = "createroom";
    private final static String JOIN_ROOM = "joinroom";
    private final static String FLIP = "flip";
    private final static String MSG = "msg";
    private final String MUTE = "mute";
    private final String UNMUTE = "unmute";


    //testing for user pm
    /* private Object User;
   // private Object List;

   // {
     //   clients = (java.util.List<ServerThread>) User;
   }*/


    public Room(String name) {
        this.name = name;
    }

    public Room(String prelobby, boolean b) {
    }

    public static void setServer(SocketServer server) {
        Room.server = server;
    }

    public String getName() {
        return name;
    }

    private List<ServerThread> clients = new ArrayList<ServerThread>();

    protected synchronized void addClient(ServerThread client) {
        client.setCurrentRoom(this);
        if (clients.indexOf(client) > -1) {
            log.log(Level.INFO, "Attempting to add a client that already exists");
        } else {
            clients.add(client);
            if (client.getClientName() != null) {
                client.sendClearList();
                sendConnectionStatus(client, true, "joined the room " + getName());
                updateClientList(client);
            }
        }
    }

    private void updateClientList(ServerThread client) {
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread c = iter.next();
            if (c != client) {
                boolean messageSent = client.sendConnectionStatus(c.getClientName(), true, null);
            }
        }
    }

    protected synchronized void removeClient(ServerThread client) {
        clients.remove(client);
        if (clients.size() > 0) {
            // sendMessage(client, "left the room");
            sendConnectionStatus(client, false, "left the room " + getName());
        } else {
            cleanupEmptyRoom();
        }
    }

    private void cleanupEmptyRoom() {
        // If name is null it's already been closed. And don't close the Lobby
        if (name == null || name.equalsIgnoreCase(SocketServer.LOBBY)) {
            return;
        }
        try {
            log.log(Level.INFO, "Closing empty room: " + name);
            close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void joinRoom(String room, ServerThread client) {
        server.joinRoom(room, client);
    }

    protected void joinLobby(ServerThread client) {
        server.joinLobby(client);
    }

    /***
     * Helper function to process messages to trigger different functionality.
     *
     * @param message The original message being sent
     * @param client  The sender of the message (since they'll be the ones
     *                triggering the actions)
     */
    private String processCommands(String message, ServerThread client) {
        String response = null;
        boolean wasCommand = false;
        try {
            if (message.indexOf(COMMAND_TRIGGER) > -1) {
                String[] comm = message.split(COMMAND_TRIGGER);
                log.log(Level.INFO, message);
                String part1 = comm[1];
                String[] comm2 = part1.split(" ");
                String command = comm2[0];
                if (command != null) {
                    command = command.toLowerCase();
                }
                String roomName;
                String clientName;
                switch (command) {
                    case CREATE_ROOM:
                        roomName = comm2[1];
                        if (server.createNewRoom(roomName)) {
                            joinRoom(roomName, client);
                        }

                        break;
                    case JOIN_ROOM:
                        roomName = comm2[1];
                        joinRoom(roomName, client);

                        break;

                    case FLIP:
                        int randFlip = (int) ((Math.random() * 2));
                        if (randFlip == 0) {
                            response = "you got <b>Heads</b>";
                            //sendMessage(client, response);
                            //System.out.println("you got heads");
                        } else {
                            response = "You got <u>Tails</u>";
                            //sendMessage(client, response);
                        }
                        break;
                        //Very broken messaging can't seem to figure it out tried numerous ways, part below is a place holder that works sometimes.
                    case MSG:
                        roomName = comm2[1];
                        response = "<b>someone wants you to join their private channel</b>" + roomName;
                        break;
                    /*Mute following a simple calling method to isMuted on serverthread to add clients name to mute list when /mute name used, appears on server but not one client
                    and no actual effect appears to occur.
                    case MUTE:
                        clientName = comm2[1];
                        if (ServerThread.isMuted(clientName)) {
                            client.muted.add(clientName);
                            response = "you've been <b>MUTED<b>";
                        }

                        break;
                    case UNMUTE:
                        clientName = comm2[1];
                        if (ServerThread.isMuted(clientName)) {
                            client.muted.remove(clientName);
                            response = "you've been <b>UNMUTED<b>";
                        }

                        break;*/
                    default:
                        response = message;
                        break;
                }

            } else {

                response = message;
                //String alteredMess = message;
                if (response.indexOf("@@") > -1) {
                    String[] s1 = response.split("@@");
                    String mess = "";
                    mess += s1[0];
                    for (int i = 1; i < s1.length; i++) {
                        if (i % 2 == 0) {
                            mess += s1[i];
                        } else {
                            mess += "<b>" + s1[i] + "</b>";
                        }
                        response = mess;
                    }

                }
                if (response.indexOf("und") > -1) {
                    String[] s1 = response.split("und");
                    String mess = "";
                    mess += s1[0];
                    for (int i = 1; i < s1.length; i++) {
                        if (i % 2 == 0) {
                            mess += s1[i];
                        } else {
                            mess += "<u>" + s1[i] + "</u>";
                        }
                        response = mess;
                    }

                }
                if (response.indexOf("colful") > -1) {
                    String[] s1 = response.split("colful");
                    String mess = "";
                    mess += s1[0];
                    for (int i = 1; i < s1.length; i++) {
                        if (i % 2 == 0) {
                            mess += s1[i];
                        } else {
                            mess += "<font color=red>" + s1[i] + "</font>";
                        }
                        System.out.println(s1[i]);
                        response = mess;
                    }

                }
                //oneattempt of sendPM
                //if (response.indexOf("@") > -1){
                //  String[] s1 = response.split("@");
                //String msg = "";

                //}

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    //Citation: (Professor Matt Toegel) some of Professor's code shown in class tinkered with to see if could get sendpm/MSG to work
   /* protected void sendM(ServerThread sender, String message){
        log.log(Level.INFO, getName() + ":sending message to" + clients.size() + " clients");
        String resp = processCommands(message, sender);
        if (resp == null){
            return;
        }
        message = resp;
        Iterator<ServerThread> iter = clients.iterator();

        while (iter.hasNext()){
            ServerThread client = iter.next();
            if (User.contains(client.getClientName())){
                boolean messageSent = client.send(sender.getClientName(), message);
                if (!messageSent){
                    iter.remove();
                }
            }
        }
    }*/

    // TODO changed from string to ServerThread
    protected void sendConnectionStatus(ServerThread client, boolean isConnect, String message) {
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread c = iter.next();
            boolean messageSent = c.sendConnectionStatus(client.getClientName(), isConnect, message);
            if (!messageSent) {
                iter.remove();
                log.log(Level.INFO, "Removed client " + c.getId());
            }
        }
    }

    /***
     * Takes a sender and a message and broadcasts the message to all clients in
     * this room. Client is mostly passed for command purposes but we can also use
     * it to extract other client info.
     *
     * @param sender  The client sending the message
     * @param message The message to broadcast inside the room
     */
    protected void sendMessage(ServerThread sender, String message) {
        log.log(Level.INFO, getName() + ": Sending message to " + clients.size() + " clients");
        String resp = processCommands(message, sender);
        if (resp == null) {
            // it was a command, don't broadcast
            return;
        }

        message = resp;
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread client = iter.next();
            boolean messageSent = client.send(sender.getClientName(), message);
            if (!messageSent) {
                iter.remove();
                log.log(Level.INFO, "Removed client " + client.getId());
            }
        }
    }

    /***
     * Will attempt to migrate any remaining clients to the Lobby room. Will then
     * set references to null and should be eligible for garbage collection
     */
    @Override
    public void close() throws Exception {
        int clientCount = clients.size();
        if (clientCount > 0) {
            log.log(Level.INFO, "Migrating " + clients.size() + " to Lobby");
            Iterator<ServerThread> iter = clients.iterator();
            Room lobby = server.getLobby();
            while (iter.hasNext()) {
                ServerThread client = iter.next();
                lobby.addClient(client);
                iter.remove();
            }
            log.log(Level.INFO, "Done Migrating " + clients.size() + " to Lobby");
        }
        server.cleanupRoom(this);
        name = null;
        // should be eligible for garbage collection now
    }

    public List<String> getRoom() {
        return server.getRooms();
    }
}