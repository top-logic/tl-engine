#!/bin/bash
# Install MCP tools for Claude Code integration.
#
# Usage: ./scripts/install-mcp-tools.sh [all|jenkins|gitea]
#
# Requirements:
#   - Python 3 with pip or uv
#   - curl (for gitea-mcp binary download)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BIN_DIR="$HOME/.local/bin"

mkdir -p "$BIN_DIR"

install_jenkins() {
    echo "=== Installing mcp-jenkins ==="
    if command -v uv &>/dev/null; then
        uv tool install mcp-jenkins
    elif command -v pip &>/dev/null; then
        pip install --user mcp-jenkins
    else
        echo "Error: Neither uv nor pip found. Install Python first." >&2
        exit 1
    fi
    echo "mcp-jenkins installed."
}

install_gitea() {
    echo "=== Installing gitea-mcp ==="
    local version="v0.7.0"
    local arch=$(uname -m)
    local os=$(uname -s)

    case "$arch" in
        x86_64) arch="x86_64" ;;
        aarch64|arm64) arch="arm64" ;;
        i386|i686) arch="i386" ;;
        *) echo "Error: Unsupported architecture: $arch" >&2; exit 1 ;;
    esac

    case "$os" in
        Linux) os="Linux" ;;
        Darwin) os="Darwin" ;;
        *) echo "Error: Unsupported OS: $os" >&2; exit 1 ;;
    esac

    local url="https://gitea.com/gitea/gitea-mcp/releases/download/${version}/gitea-mcp_${os}_${arch}.tar.gz"
    echo "Downloading from: $url"

    curl -fsSL "$url" | tar -xzf - -C "$BIN_DIR" gitea-mcp
    chmod +x "$BIN_DIR/gitea-mcp"

    echo "gitea-mcp installed to $BIN_DIR/gitea-mcp"
}

install_python_deps() {
    echo "=== Installing Python dependencies (keyring) ==="
    if command -v pip3 &>/dev/null; then
        pip3 install --user keyring
    elif command -v pip &>/dev/null; then
        pip install --user keyring
    else
        echo "Warning: pip not found. Install keyring manually: pip install keyring" >&2
    fi
}

case "${1:-all}" in
    jenkins)
        install_jenkins
        ;;
    gitea)
        install_gitea
        ;;
    all)
        install_python_deps
        install_jenkins
        install_gitea
        ;;
    *)
        echo "Usage: $0 [all|jenkins|gitea]"
        exit 1
        ;;
esac

echo
echo "Done. Now run: ./scripts/setup-mcp-credentials.sh"
