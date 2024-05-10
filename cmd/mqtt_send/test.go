package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"os"
	"time"

	"crypto/tls"
	"crypto/x509"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

const (
	// https://www.hivemq.com/blog/mqtt-essentials-part-6-mqtt-quality-of-service-levels/
	AtMostOnce  = 0
	AtLeastOnce = 1
	ExactlyOnce = 2
)

type (
	// DataMap is a generic name/value pair map with all strings
	DataMap map[string]string

	// DataFrame is a generic 'envelop' for sending telemetry data to the cloud
	DataFrame struct {
		DeviceID string  `json:"deviceid"`        // any identifier of the device, e.g. a hostname, serial number etc.
		Batch    int64   `json:"batch,omitempty"` // a prefix to group telemetry data
		Type     int     `json:"type"`            // the type of the payload: BLOB or KV
		Data     DataMap `json:"data,omitempty"`  // data in a key/value set
		Blob     string  `json:"blob,omitempty"`  // binary data in base64 encoded string
		TS       int64   `json:"ts"`              // the timestamp the message was sent
	}
)

func newTLSConfig() *tls.Config {
	// Import trusted certificates from CAfile.pem.
	// Alternatively, manually add CA certificates to
	// default openssl CA bundle.
	certpool := x509.NewCertPool()
	pemCerts, err := os.ReadFile("ca.crt")
	if err != nil {
		panic(err)
	}
	certpool.AppendCertsFromPEM(pemCerts)

	// Create tls.Config with desired tls properties
	return &tls.Config{
		// RootCAs = certs used to verify server cert.
		RootCAs: certpool,
		// ClientAuth = whether to request cert from server.
		// Since the server is set up for SSL, this happens anyways.
		ClientAuth: tls.NoClientCert,
		// ClientCAs = certs used to validate client cert.
		ClientCAs: nil,
		// InsecureSkipVerify = verify that cert contents match server. IP matches what is in cert etc.
		InsecureSkipVerify: true,
		// Certificates = list of certs client sends to server.
		//Certificates: []tls.Certificate{cert},
	}
}

func onConnect(c mqtt.Client) {
	fmt.Println("connected.")
}

var f mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {
	fmt.Printf("TOPIC: %s\n", msg.Topic())
	fmt.Printf("MSG: %s\n", msg.Payload())
}

func main() {

	// command line parameters
	var host string
	var port string
	var protocol string
	var queue string
	var user string
	var password string
	var secure bool
	n := 100

	// get command line options
	flag.StringVar(&host, "host", "localhost", "Endpoint")
	flag.StringVar(&port, "port", "1883", "Default port")
	flag.StringVar(&protocol, "protocol", "tcp", "Default protocol")
	flag.StringVar(&queue, "queue", "test", "Default queue")
	flag.StringVar(&password, "password", "", "Password (optional)")
	flag.StringVar(&user, "user", "", "User name (optional)")
	flag.BoolVar(&secure, "secure", false, "Use certificate (ca.cert) & TLS")
	flag.Parse()

	// setup and configuration
	broker := fmt.Sprintf("%s://%s:%s", protocol, host, port)
	fmt.Println("Connecting to " + broker)

	opts := mqtt.NewClientOptions().AddBroker(broker)
	opts.SetCleanSession(true)
	opts.SetClientID("test")
	opts.SetConnectTimeout(10 * time.Second)
	opts.SetOnConnectHandler(onConnect)
	opts.SetDefaultPublishHandler(f)

	//opts.SetKeepAlive(30 * time.Second)
	//opts.SetPingTimeout(30 * time.Second)
	if user != "" {
		opts.SetUsername(user)
	}
	if password != "" {
		opts.SetPassword(password)
	}
	if secure {
		opts.SetTLSConfig(newTLSConfig())
	}

	// create a client
	c := mqtt.NewClient(opts)
	if token := c.Connect(); token.Wait() && token.Error() != nil {
		c.Disconnect(250)
		panic(token.Error())
	}
	defer c.Disconnect(250)

	df := DataFrame{
		DeviceID: "test",
		Batch:    1,
		Type:     1,
	}
	payload, err := json.Marshal(df)
	if err == nil {
		for i := 0; i < n; i++ {
			if token := c.Publish(queue, AtMostOnce, false, payload); token.Wait() && token.Error() != nil {
				panic(token.Error())
			}
			fmt.Println("sent ...")
			time.Sleep(10 * time.Second)
		}

	}

	fmt.Println("done.")
}
