# Wikipedia-mediator Project

This was the third of three mini-projects done for a my CPEN221 course at UBC, I've listed my contributions to the project below

- **Implementation of a Buffer**
  - Using the java.time library to implement a buffer that stores an item until it becomes "stale" and obsolete
  - Writing some basic methods for the buffer that allows client to iteract with items inside
  - NOTE: the task of ensuring the thread safety of this class was handled by my two teammates, and I did not make any contributions towards making this class thread-safe

- **Implementation of a mediator class**
  - Using the JWiki api to implement a class to handle requests to the wikipedia API and information coming from the wikipedia API
  - Also implementing package-private helper classes to handle and parse information obtained through the wikipedia API and stored within the mediator class
  - NOTE: once again, I made no contributions towards making this class thread-safe, a task that was left to my two teammates

- **Concurrent breadth-first-search**
  - Using the java concurrent library to implement a simple breadth-first-search that runs using multiple threads for improved performance
  - Using the java concurrent library to implement a timeout feature for this method:
    - Given a timeout parameter, the method will end if it does not compelete within the allocated time and throw a timeoutException
  - This method is tasked with discovering the shortest path between two Wikipedia pages, akin to the project "six degrees of wikipedia"

A more detailed account of contributions exist in the form of the three CONTRIB-(github_username).md files
