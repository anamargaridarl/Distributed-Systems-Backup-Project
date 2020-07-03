# sdis1920-t6g21
## README

The java version used in the development of the project is 13.0.2

### Program Setup
To setup a test environment of the project, simply run the ***build.sh*** script. It will compile the code and create a directory **test** where the compiled class files can be found.

### Testing
Inside the **test** folder there will be 5 folders, 4 of them are for running a peer, and 1 for running the client application.

We implemented some scripts to easily start the peers. For that:
 1. Run the initial_peer.sh <terminal>
 2. Run the launch_peers.sh <terminal> 
 3. If you want to add a new peer run the new_peer.sh <terminal>
 
 **Note**: <terminal> argument corresponds to the terminal used in you OS.

Another possible way is to start each peer individually. For that you can execute the commands present in the file ***commands.md*** inside the **docs** folder, pop up a terminal on each folder.The commands file give examples on how to execute the program and test it.

