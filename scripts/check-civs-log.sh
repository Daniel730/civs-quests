#!/usr/bin/env bash
HOST=daniel@bot-server
LOG=/home/daniel/mineserver/logs/latest.log
echo "=== enable/done ==="
ssh -o BatchMode=yes "$HOST" "grep -inE 'Enabling Civs|Enabling RPGServer|Done \(|For help, type' $LOG | tail -n 15"
echo "=== civs enable context ==="
ssh -o BatchMode=yes "$HOST" "grep -inA1 'Enabling Civs' $LOG | tail -n 10"
echo "=== errors ==="
ssh -o BatchMode=yes "$HOST" "grep -inE 'ERROR|SEVERE|Exception|Caused by|Could not load|Disabling' $LOG | tail -n 20"
echo "=== proc ==="
ssh -o BatchMode=yes "$HOST" "pgrep -af server.jar | grep java || echo NO_JAVA"