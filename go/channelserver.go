package main

import (
	"crypto/rand"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"github.com/samegoal/wc"
	"log"
	"net/http"
	"sync"
)

var (
	// The session manager instance.
	htsm *helloTimerSessionManager
)

const (
	csp = "default-src 'none'; script-src 'self'; img-src 'self'; " +
		"connect-src 'self'; style-src 'self' 'unsafe-inline'; " +
		"sandbox allow-forms allow-popups allow-same-origin allow-scripts;"
)


// Create a new session id as an hex-encoded random 8-byte sequence.
func newSID() string {
	b := make([]byte, 8)
	rand.Read(b)
	d := make([]byte, hex.EncodedLen(len(b)))
	hex.Encode(d, b)
	return string(d)
}

type helloTimerSession struct {
	*wc.DefaultSession
	messages []*wc.Message
	bid, fid int
	user string
}

// Return true if the session has been authenticated.
func (s *helloTimerSession) Authenticated(r *http.Request) bool {
	return s.SID() != ""
}

func (s *helloTimerSession) BackChannelACKThrough(ID int) error {
	// Obtain sync access to the session manager
	htsm.mutex.Lock()
	defer htsm.mutex.Unlock()

	// Iterate slices of the messages array?  What is this
	// accomplishing?
	for len(s.messages) > 0 {
		if s.messages[0].ID > ID {
			break
		}
		s.messages = s.messages[1:]
	}
	return nil
}

// Given a session object, send the list of messages.
func (s *helloTimerSession) ForwardChannel(msgs []*wc.Message) error {

	// Sync access to session manager
	htsm.mutex.Lock()
	defer htsm.mutex.Unlock()

	// Iterate all messages in the list
	for _, msg := range msgs {

		// Map literal constructor for: string -> string map (for json?)
		j := map[string]string{}

		// Unmarshal the msg.Body json into the map
		err := json.Unmarshal(msg.Body, &j)
		if err != nil {
			return err
		}

		// Switch on the message type
		user, hasUser := j["user"]
		message, hasMessage := j["message"]
		bcastMsg := []interface{}{}
		switch {
		case hasUser:
			bcastMsg = []interface{}{"new", user}
			s.user = user
		case hasMessage:
			bcastMsg = []interface{}{"message", s.user, message}
		default:
			return fmt.Errorf("Unsupported message type")
		}
		bcastJSON, err := json.Marshal(bcastMsg)
		if err != nil {
			panic(err)
		}
		for _, bcastSession := range htsm.sessionMap {
			cs := bcastSession.(*helloTimerSession)
			finalMsg := wc.NewMessage(cs.bid, bcastJSON)
			cs.bid++
			cs.messages = append(cs.messages, finalMsg)
			cs.DataNotifier() <- len(finalMsg.Body)
		}
	}

	return nil
}

func (s *helloTimerSession) BackChannelPeek() ([]*wc.Message, error) {
	htsm.mutex.Lock()
	defer htsm.mutex.Unlock()

	return s.messages, nil
}

func (s *helloTimerSession) BackChannelAdd(messageBody []byte) error {
	htsm.mutex.Lock()
	defer htsm.mutex.Unlock()

	msg := wc.NewMessage(s.bid, messageBody)
	s.bid++
	s.messages = append(s.messages, msg)
	return nil
}

func newHelloTimerSession(r *http.Request) *helloTimerSession {
	return &helloTimerSession{DefaultSession: wc.NewDefaultSession(r, newSID())}
}

type helloTimerSessionManager struct {
	wc.DefaultSessionManager
	mutex      sync.Mutex
	sessionMap map[string]wc.Session
}

func (htsm *helloTimerSessionManager) NewSession(r *http.Request) (wc.Session, error) {
	htsm.mutex.Lock()
	defer htsm.mutex.Unlock()
	s := newHelloTimerSession(r)
	htsm.sessionMap[s.SID()] = s
	return s, nil
}

func (htsm *helloTimerSessionManager) TerminatedSession(s wc.Session, reason wc.TerminationReason) error {
	htsm.mutex.Lock()
	defer htsm.mutex.Unlock()

	session := s.(*helloTimerSession)
	delete(htsm.sessionMap, session.SID())

	bcastMsg := []interface{}{"delete", session.user}
	bcastJSON, err := json.Marshal(bcastMsg)
	if err != nil {
		panic(err)
	}
	for _, bcastSession := range htsm.sessionMap {
		cs := bcastSession.(*helloTimerSession)
		finalMsg := wc.NewMessage(cs.bid, bcastJSON)
		cs.bid++
		cs.messages = append(cs.messages, finalMsg)
		cs.DataNotifier() <- len(finalMsg.Body)
	}
	return nil
}

type appHandler func(http.ResponseWriter, *http.Request) error

func (fn appHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	//addCommonHeaders(w)
	if err := fn(w, r); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}

	header := w.Header()
	// http://caniuse.com/#search=content%20security%20policy
	// X-Content-Security-Policy for Firefox until version 23 and IE10&11
	// X-Webkit-CSP for Chrome until version 25 and Safari until version 7
	// Content-Security-Policy for Chrome 25+, Firefox 23+, Safari 7+
	header.Set("X-Content-Security-Policy", csp)
	header.Set("X-Webkit-CSP", csp)
	header.Set("Content-Security-Policy", csp)

}

func helloTimerHTMLHandler(w http.ResponseWriter, r *http.Request) error {
	if r.URL.Path != "/" {
		http.NotFound(w, r)
		return nil
	}
	return nil
	//return chatTemplate.Execute(w, ChatPage{*depsPath == ""})
}

func StartWcHttpHandler() {
	// Create a session manager for wc
	htsm = &helloTimerSessionManager{sessionMap: make(map[string]wc.Session)}

	// Register this callback
	wc.SetSessionManager(htsm)

	// App handler implementtion
	http.Handle("/", appHandler(helloTimerHTMLHandler))

	log.Printf("Http server started")

}
