# Snake Lake

Multiplayer snake!

Play: http://timothypratley.github.io/snakelake

Screencast: https://www.youtube.com/watch?v=3NZJjwv6yy0


## Overview

Avoid colliding with other snakes.
Invite your friends to battle.

## Development

To run the server

    lein run

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3000](http://localhost:3000/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

To clean all compiled files:

    lein clean

To create a production build run:

    lein with-profile uberjar do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.

## Deploy

To deploy the server:

    git push heroku master

To deploy the static assets:

    ./deploy.sh

## License

Copyright © 2016 Timothy Pratley

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
