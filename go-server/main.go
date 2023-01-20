package main

import (
	"fmt"
	"net/http"

	"github.com/gobwas/ws"
	"github.com/gobwas/ws/wsutil"
)

func main() {
	fmt.Println("Server started, waiting for connection on port 8080...")
	http.ListenAndServe(":8080", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		fmt.Println("Client connected")
		conn, _, _, err := ws.UpgradeHTTP(r, w)
		if err != nil {
			fmt.Println("Error upgrading connection:", err)
			return
		}
		go func() {
			defer conn.Close()
			for {
				msg, op, err := wsutil.ReadClientData(conn)
				if err != nil {
					fmt.Println("Error reading message:", err)
					return
				}

				fmt.Println("Received:", string(msg))
				err = wsutil.WriteServerMessage(conn, op, []byte("Hello. From server"))
				fmt.Println("Sent: Hello. From server")
				if err != nil {
					fmt.Println("Error writing message:", err)
					return
				}
			}
		}()
	}))
}
