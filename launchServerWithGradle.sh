#!/bin/sh

# Utility script that launch the ledger server using gradle 
# to setup CLASSPATH, compile sources, ... for git sources

gradle :java-ilp-ledger-api:launchServer
