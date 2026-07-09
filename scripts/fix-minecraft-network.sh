#!/usr/bin/env bash
# Gateway, fixed LAN IP 192.168.1.252, and DuckDNS refresh.
# Run: sudo ~/fix-minecraft-network.sh
#
# Note: hostname -I may show 192.168.1.50 AND 192.168.1.252 on enp4s0. Older docs used .50
# because it was the primary NM address. Router port-forward must target 192.168.1.252:25565.
set -euo pipefail

TARGET_GATEWAY="192.168.1.1"
TARGET_LAN_IP="192.168.1.252"
TARGET_PREFIX="24"
TARGET_DNS="192.168.1.1 8.8.8.8"
MINECRAFT_PORT="25565"
DUCKDNS_DOMAIN="forever-server"
DUCKDNS_TOKEN_FILE="/etc/duckdns.token"

echo "=== Minecraft / DuckDNS network fix ==="
echo ""

if [[ "${EUID:-$(id -u)}" -ne 0 ]]; then
  echo "This script must be run as root. Use: sudo $0"
  exit 1
fi

IFACE=""
if ip link show enp4s0 &>/dev/null; then
  IFACE="enp4s0"
else
  while IFS= read -r name; do
    [[ "$name" == "lo" ]] && continue
    if ip -4 addr show "$name" 2>/dev/null | grep -q 'inet 192\.168\.1\.'; then
      IFACE="$name"
      break
    fi
  done < <(ip -o link show | awk -F': ' '{print $2}' | cut -d@ -f1)
fi

if [[ -z "$IFACE" ]]; then
  echo "ERROR: No LAN interface on 192.168.1.x"
  exit 1
fi

echo "Interface: ${IFACE}"
CONN=$(nmcli -t -f NAME,DEVICE connection show --active | awk -F: -v dev="$IFACE" '$2==dev {print $1; exit}')
if [[ -z "$CONN" ]]; then
  CONN=$(nmcli -t -f GENERAL.CONNECTION device show "$IFACE" 2>/dev/null | cut -d: -f2 | tr -d ' ')
fi
if [[ -z "$CONN" ]]; then
  echo "ERROR: No NetworkManager connection for ${IFACE}"
  exit 1
fi

echo "NM connection: ${CONN}"
CURRENT_GW=$(nmcli -g ipv4.gateway connection show "$CONN" 2>/dev/null || true)
CURRENT_METHOD=$(nmcli -g ipv4.method connection show "$CONN" 2>/dev/null || true)
echo "ipv4.method: ${CURRENT_METHOD:-?} | gateway: ${CURRENT_GW:-<unset>}"

echo "Setting static ${TARGET_LAN_IP}/${TARGET_PREFIX}, gateway ${TARGET_GATEWAY}..."
nmcli connection modify "$CONN" \
  ipv4.method manual \
  ipv4.addresses "${TARGET_LAN_IP}/${TARGET_PREFIX}" \
  ipv4.gateway "$TARGET_GATEWAY" \
  ipv4.dns "$TARGET_DNS"
nmcli connection down "$CONN" || true
nmcli connection up "$CONN"
echo "Connection reactivated."

echo ""
echo "IPv4 on ${IFACE}:"
ip -4 addr show dev "$IFACE" | grep inet || true
echo "Default route:"
ip -4 route show default || true

echo ""
echo "Public IPv4 (ipv4.icanhazip.com)..."
if PUBLIC_IP=$(curl -4 -fsS --max-time 15 https://ipv4.icanhazip.com 2>/dev/null | tr -d '\r\n'); then
  echo "Public IPv4: ${PUBLIC_IP}"
else
  echo "WARNING: could not reach ipv4.icanhazip.com — check gateway/DNS."
fi

echo ""
echo "=== DuckDNS (${DUCKDNS_DOMAIN}) ==="
if [[ -f "$DUCKDNS_TOKEN_FILE" ]]; then
  TOKEN=$(tr -d ' \t\r\n' < "$DUCKDNS_TOKEN_FILE")
  if [[ -z "$TOKEN" ]]; then
    echo "ERROR: empty token in ${DUCKDNS_TOKEN_FILE}"
    exit 1
  fi
  RESP=$(curl -fsS --max-time 30 "https://www.duckdns.org/update?domains=${DUCKDNS_DOMAIN}&token=${TOKEN}&ip=" || true)
  echo "DuckDNS response: ${RESP:-<no response>}"
else
  echo "Missing ${DUCKDNS_TOKEN_FILE} — create it with your DuckDNS token, then re-run."
fi

echo ""
echo "=== Verification ==="
echo "  1. ping -c 2 ${TARGET_GATEWAY}"
echo "  2. ss -tlnp | grep ${MINECRAFT_PORT}   (Minecraft on *:${MINECRAFT_PORT})"
echo "  3. dig +short ${DUCKDNS_DOMAIN}.duckdns.org @8.8.8.8  (should match public IPv4)"
echo "  4. Router CHITA-Hub5: TCP ${MINECRAFT_PORT} WAN -> ${TARGET_LAN_IP}:${MINECRAFT_PORT} (NOT 192.168.1.50)"
echo "  5. Minecraft address (internet): ${DUCKDNS_DOMAIN}.duckdns.org  (default port ${MINECRAFT_PORT} — no :port in client)"
echo "  6. On same Wi-Fi/LAN: use ${TARGET_LAN_IP} or enable router NAT hairpin for DuckDNS"
echo "  7. If whitelist=true, add nick to whitelist.json"
echo ""
echo "Done."
