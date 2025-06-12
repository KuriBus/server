import StartScene from './StartScene.js';
import NicknameScene from './NicknameScene.js';
import CharacterSelectScene from './CharacterSelectScene.js';
import WorldMapScene from './WorldMapScene.js';
import MainScene from './mainScene.js';
import BridgeScene from './BridgeScene.js';

import { Client } from 'https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.1/+esm';

const config = {
  type: Phaser.AUTO,
  width: 1600,
  height: 900,
  parent: 'game-container',
  physics: {
    default: 'arcade',
    arcade: { debug: false }
  },
  dom: {
    createContainer: true
  },
  scene: [
    StartScene,
    NicknameScene,
    CharacterSelectScene,
    WorldMapScene,
    MainScene,
    BridgeScene
  ]
};

const game = new Phaser.Game(config);

const socket = new SockJS(WS_URL); 

const stompClient = new Client({
  webSocketFactory: () => socket,
  reconnectDelay: 5000,
  debug: (str) => console.log('[STOMP]', str)
});

stompClient.onConnect = () => {
  console.log("STOMP WebSocket 연결 성공");
};

stompClient.onStompError = (frame) => {
  console.error("STOMP 오류:", frame.headers['message']);
};

stompClient.activate();

export { stompClient, socket };