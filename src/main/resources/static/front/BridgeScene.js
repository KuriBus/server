import { stompClient } from './game.js';

const SERVER_URL = 'https://kuriverse.shop';

class BridgeScene extends Phaser.Scene {
  constructor() {
    super('BridgeScene');
    this.currentDirection = null;
    this.moveInterval = null;
    this.otherPlayers = new Map();
    this.bodytypeMap = new Map();
    this.activeBubble = null;
    this.activePortal = null;
    this.chatInputField = null;
    this.customizationSub = null;
    this.positionsSub = null;
    this.chatSub = null;
  }

  init(data) {
    if (data && data.userInfo) {
      window.userInfo = data.userInfo;
    }
    this.roomId = window.userInfo?.roomId || 4;
    this.character = window.userInfo?.character || 'boy1';
    this.nickname = window.userInfo?.nickname || '사용자';
  }

  preload() {
    this.load.audio('bridgeBgm', 'assets/audio/bridgebgm.mp3');
    const characterList = ['boy1', 'boy2', 'boy3', 'girl1', 'girl2', 'girl3'];
    characterList.forEach(key => {
      if (!this.textures.exists(key)) this.load.image(key, `assets/${key}.png`);
    });

    const bgMap = { 4: 'bridge3', 5: 'bridge2', 6: 'bridge1' };
    const bgKey = bgMap[this.roomId] || 'bridge1';
    if (!this.textures.exists(bgKey)) {
      this.load.image(bgKey, `assets/${bgKey}.png`);
    }
    this.bgKeyToUse = bgKey;
  }

  isStompConnected() {
    return stompClient && stompClient.active;
  }

  create() {
    this.bgm = this.sound.add('bridgeBgm', { loop: true, volume: 0.1 });
    this.bgm.play();
    this.input.keyboard.enabled = true;
    this.otherPlayers.clear();
    this.bodytypeMap.clear();
    this.roomNameMap = { 4: "통로 1", 5: "통로 2", 6: "통로 3" };
    this.currentRoomName = this.roomNameMap[this.roomId] || "알 수 없는 통로";

    this.events.on('shutdown', this.shutdown, this);

    this.input.keyboard.on('keydown', this.handleKeyDown, this);
    this.input.keyboard.on('keyup', this.handleKeyUp, this);

    this.add.image(800, 450, this.bgKeyToUse).setDisplaySize(1600, 900).setDepth(0);
    const titleText = this.currentRoomName;
    this.add.rectangle(800, 50, 300, 60, 0xB593CC).setDepth(5).setStrokeStyle(2, 0xffffff);
    this.add.text(800, 50, titleText, { fontSize: '32px', fontFamily: 'Pretendard', color: '#ffffff' }).setOrigin(0.5).setDepth(6);
    this.add.rectangle(300, 750, 580, 200, 0x000000, 0.4).setDepth(2);

    this.chatLogContainer = this.add.dom(300, 730).createFromHTML(`
  <div style="position: relative;">
    <style>
      #chat-log-box::-webkit-scrollbar { width: 6px; }
      #chat-log-box::-webkit-scrollbar-track { background: transparent; }
      #chat-log-box::-webkit-scrollbar-thumb { background-color: #B593CC; border-radius: 3px; }
    </style>
    <div id="chat-log-box" style="width: 534px; height: 180px; overflow-y: auto; background: rgba(0,0,0,0); color: white; font-size: 16px; font-family: Pretendard, sans-serif; padding: 10px; box-sizing: border-box; scrollbar-width: thin; scrollbar-color: #B593CC transparent;"></div>
  </div>
`).setOrigin(0.5).setDepth(6);

    this.chatInput = this.add.dom(300, 850).createFromHTML(`
  <div style="width: 534px; height: 58px; background: #fff; border: 3px solid #B593CC; border-radius: 12px; display: flex; align-items: center; padding: 0 25px; gap: 13px;">
    <input id="chat-message" type="text" placeholder="메시지를 입력하세요" style="flex: 1; border: none; outline: none; font-size: 16px;" />
    <button id="send-btn" style="width: 68px; height: 58px; background: #B593CC; border-radius: 12px; border: none; color: #fff; font-weight: bold;">→</button>
  </div>
`).setOrigin(0.5).setDepth(10);

    this.chatInput.on('create', (dom) => {
      this.chatInputField = dom.getChildByID('chat-message');
      const sendButton = dom.getChildByID('send-btn');
      const sendMessage = () => {
        const message = this.chatInputField.value.trim();
        if (message) {
          this.addChatLog(`${this.nickname}: ${message}`);
          this.chatInputField.value = '';
          this.createBubble(message);
        }
        this.chatInputField.blur();
      };

      this.chatInputField.addEventListener('focus', () => this.input.keyboard.enabled = false);
      this.chatInputField.addEventListener('blur', () => this.input.keyboard.enabled = true);
      this.chatInputField.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') sendMessage();
      });
      sendButton.addEventListener('click', sendMessage);
    });

    this.addChatLog = (text) => {
      const chatBox = this.chatLogContainer.getChildByID('chat-log-box');
      if (chatBox) {
        const p = document.createElement('p');
        p.textContent = text;
        p.style.margin = '0 0 6px 0';
        chatBox.appendChild(p);
        chatBox.scrollTop = chatBox.scrollHeight;
      }
    };

    // 채팅 입력 이벤트 처리
    this.time.delayedCall(100, () => {
      const chatInputField = this.chatInput.getChildByID('chat-message');
      const sendBtn = this.chatInput.getChildByID('send-btn');
      // 채팅 전송 함수
      const sendMessage = () => {
        const message = chatInputField.value.trim();
        if (message && this.isStompConnected()) {
          stompClient.publish({
            destination: '/app/chat.send',
            body: JSON.stringify({
              roomId: this.roomId,
              nickname: this.nickname,
              content: message
            })
          });
          chatInputField.value = '';
        } else if (!this.isStompConnected()) {
          this.addChatLog('[시스템] 연결이 끊어졌습니다. 잠시 후 다시 시도해주세요.');
        }
        if (chatInputField.value.trim() === '') {
          setTimeout(() => {
            chatInputField.blur();
          }, 10); // 약간의 딜레이를 주어 blur가 정상 동작하도록
        }
      };
      // 엔터키 전송
      chatInputField.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') {
          event.preventDefault();
          sendMessage();
        }
      });
      // 버튼 클릭 전송
      sendBtn.addEventListener('click', sendMessage);

      // 입력창 포커스/블러에 따라 게임 키 입력 활성/비활성
      chatInputField.addEventListener('focus', () => this.input.keyboard.enabled = false);
      chatInputField.addEventListener('blur', () => this.input.keyboard.enabled = true);
    });

    this.player = this.physics.add.sprite(800, 650, this.character).setDisplaySize(100, 120).setCollideWorldBounds(true).setOrigin(0.5);
    this.nicknameBg = this.add.rectangle(this.player.x, this.player.y + 78, 100, 22, 0x000000, 0.4).setOrigin(0.5).setDepth(5);
    this.nicknameText = this.add.text(this.player.x, this.player.y + 78, this.nickname, { font: '14px Pretendard', fill: '#ffffff' }).setOrigin(0.5).setDepth(6);

    // 포털 이동키
    this.eKey = this.input.keyboard.addKey('E');
    this.createPortals(this.roomId);

    window.addEventListener('beforeunload', () => {
      if (stompClient.active) {
        this.leaveRoom(this.roomId, this.nickname);
        stompClient.deactivate();
      }
      // 로그아웃 API 호출
      navigator.sendBeacon('/api/users/logout');
    });

    this.initWebSocket(this.roomId, this.nickname);

    const characterList = ['boy1', 'boy2', 'boy3', 'girl1', 'girl2', 'girl3'];
    const npcCount = Phaser.Math.Between(5, 6);

    for (let i = 0; i < npcCount; i++) {
      const key = Phaser.Utils.Array.GetRandom(characterList);
      const x = Phaser.Math.Between(100, 1500);
      const y = Phaser.Math.Between(600, 880);

      const npc = this.add.sprite(x, y, key)
      .setDisplaySize(100, 120)
      .setDepth(1);
      npc.setFlipX(Math.random() < 0.5); // 랜덤 방향
    }
  }

  async joinRoom(roomId, nickname) {
    try {
      await fetch(`${SERVER_URL}/api/rooms/${roomId}/join`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nickname })
      });
      console.log('방 입장 성공');
    } catch (e) {
      console.error('방 입장 오류:', e);
    }
  }

  async leaveRoom(roomId, nickname) {
    try {
      await fetch(`${SERVER_URL}/api/rooms/${roomId}/leave`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nickname: nickname })
      });
      console.log("방 퇴장 완료");
    } catch (error) {
      console.error("퇴장 오류:", error);
    }
  }

  async getAllCustomizations() {
    try {
      const response = await fetch(`${SERVER_URL}/api/customization/all`);
      if (!response.ok) {
        throw new Error(`전체 외형 정보 로딩 실패: ${response.status}`);
      }
      const allCustoms = await response.json();
      allCustoms.forEach(custom => {
        const bodytype = custom.bodytype || custom.bodyType;
        this.bodytypeMap.set(custom.nickname, bodytype);
      });
    } catch (e) {
      console.error('전체 외형 정보 로딩 중 오류:', e);
    }
  }

  async initWebSocket(roomId, nickname) {
    await this.joinRoom(roomId, nickname);
    await this.getAllCustomizations();

    if (stompClient.active) {
      this.setupSubscriptions();
      stompClient.publish({ destination: "/app/move", body: JSON.stringify({ nickname, direction: "init", roomId, x: 800 / 40, y: 650 / 40 }) });
    } else {
      stompClient.onConnect = () => {
        this.setupSubscriptions();
        stompClient.publish({ destination: "/app/move", body: JSON.stringify({ nickname, direction: "init", roomId, x: 800 / 40, y: 650 / 40 }) });
      };
    }
  }

  setupSubscriptions() {
    const bodytypeToCharacter = { 1: 'boy1', 2: 'boy2', 3: 'boy3', 4: 'girl1', 5: 'girl2', 6: 'girl3' };

    if (this.chatSub) this.chatSub.unsubscribe();
    if (this.positionsSub) this.positionsSub.unsubscribe();
    if (this.customizationSub) this.customizationSub.unsubscribe();

    this.customizationSub = stompClient.subscribe('/topic/customization', (message) => {
      const data = JSON.parse(message.body);
      const bodytype = data.bodytype || data.bodyType;
      this.bodytypeMap.set(data.nickname, bodytype);
      if (this.otherPlayers.has(data.nickname)) {
        const player = this.otherPlayers.get(data.nickname);
        const charKey = bodytypeToCharacter[bodytype] || 'boy1';
        player.sprite.setTexture(charKey);
      }
    });

    this.positionsSub = stompClient.subscribe('/topic/positions', (message) => {
      const positions = JSON.parse(message.body);
      const usersInSameRoom = positions.filter(pos => pos.roomName === this.currentRoomName);
      const receivedNicknames = new Set(usersInSameRoom.map(p => p.nickname));
      const myData = positions.find(pos => pos.nickname === this.nickname);
      if (myData && this.player) {
        const SCALE = 16;
        this.player.setPosition(myData.x * SCALE, myData.y * SCALE);
      }
      usersInSameRoom.forEach(pos => {
        if (pos.nickname === this.nickname) return;
        const SCALE = 16;
        const targetX = pos.x * SCALE;
        const targetY = pos.y * SCALE;
        if (!this.otherPlayers.has(pos.nickname)) {
          const bodytype = this.bodytypeMap.get(pos.nickname);
          const charKey = bodytypeToCharacter[bodytype] || 'boy1';
          const otherSprite = this.physics.add.sprite(targetX, targetY, charKey).setDisplaySize(100, 120);
          const nicknameBg = this.add.rectangle(targetX, targetY + 78, 100, 22, 0x000000, 0.4).setOrigin(0.5).setDepth(5);
          const nicknameText = this.add.text(targetX, targetY + 78, pos.nickname, { font: '14px Pretendard', fill: '#ffffff' }).setOrigin(0.5).setDepth(6);
          this.otherPlayers.set(pos.nickname, { sprite: otherSprite, nicknameBg, nicknameText, chatBubble: null });
        } else {
          const playerObj = this.otherPlayers.get(pos.nickname);
          playerObj.sprite.setPosition(targetX, targetY);
        }
      });
      this.otherPlayers.forEach((playerData, nick) => {
        if (!receivedNicknames.has(nick)) {
          playerData.sprite.destroy(); playerData.nicknameBg.destroy(); playerData.nicknameText.destroy();
          this.otherPlayers.delete(nick); this.bodytypeMap.delete(nick);
        }
      });
    });

    stompClient.subscribe(`/queue/warnings/${this.nickname}`, (msg) => {
    this.addChatLog(`[⚠️ 경고] ${msg.body}`);
  });

  // 에러 메시지 구독 추가
  stompClient.subscribe(`/queue/errors/${this.nickname}`, (msg) => {
    this.addChatLog(`[❌ 에러] ${msg.body}`);
  });
    
    this.chatSub = stompClient.subscribe(`/topic/room/${this.roomId}`, (msg) => {
      let chat;
      try {
        chat = JSON.parse(msg.body);
      } catch (e) {
        chat = { nickname: '시스템', content: msg.body };
      }
      const sender = chat.nickname;
      const content = chat.content;
      this.addChatLog(`[${sender}] ${content}`);
      // 자신의 말풍선 표시
      if (sender === this.nickname) {
        this.showChatBubble(this.player, content, true);
      }
      // 타 플레이어의 말풍선 표시
      else if (this.otherPlayers.has(sender)) {
        this.showChatBubble(this.otherPlayers.get(sender).sprite, content, false, sender);
      }
    });
  }

  showChatBubble(targetSprite, message, isMe, nickname = null) {
    // 자신의 말풍선 or 타 플레이어 말풍선이 이미 있으면 제거
    if (isMe && this.activeBubble) {
      this.activeBubble.destroy();
      this.activeBubble = null;
    }
    if (!isMe && nickname && this.otherPlayers.get(nickname)?.chatBubble) {
      this.otherPlayers.get(nickname).chatBubble.destroy();
      this.otherPlayers.get(nickname).chatBubble = null;
    }

    const bubbleWidth = 220;
    const bubbleHeight = 90;
    const tailSize = 12;

    // 말풍선 박스
    const bubbleRect = this.add.graphics();
    bubbleRect.fillStyle(0xffffff, 1);
    bubbleRect.fillRoundedRect(0, 0, bubbleWidth, bubbleHeight, 10);

    // 말풍선 꼬리
    const tail = this.add.graphics();
    tail.fillStyle(0xffffff, 1);
    tail.beginPath();
    tail.moveTo(bubbleWidth / 2 - tailSize, bubbleHeight);
    tail.lineTo(bubbleWidth / 2, bubbleHeight + tailSize);
    tail.lineTo(bubbleWidth / 2 + tailSize, bubbleHeight);
    tail.closePath();
    tail.fillPath();

    // 메시지 텍스트
    const msgText = this.add.text(bubbleWidth / 2, bubbleHeight / 2, message, {
      font: '14px Pretendard',
      color: '#000000',
      align: 'center',
      wordWrap: { width: bubbleWidth - 20 }
    }).setOrigin(0.5);

    // 말풍선 컨테이너 (타겟 스프라이트 기준 위치)
    const bubble = this.add.container(
      targetSprite.x - bubbleWidth / 2,
      targetSprite.y - 150,
      [bubbleRect, tail, msgText]
    ).setDepth(6);

    // 3초 후 말풍선 제거
    this.time.delayedCall(3000, () => {
      bubble.destroy();
      if (isMe) this.activeBubble = null;
      else if (nickname && this.otherPlayers.get(nickname)) {
        this.otherPlayers.get(nickname).chatBubble = null;
      }
    });

    // 자신 or 타 플레이어 말풍선 저장
    if (isMe) this.activeBubble = bubble;
    else if (nickname && this.otherPlayers.get(nickname)) {
      this.otherPlayers.get(nickname).chatBubble = bubble;
    }
  }

  createPortals(roomId) {
    const portalConfig = {
      4: [{ x: 100, y: 670, target: { scene: 'MainScene', roomId: 1 } }, { x: 1500, y: 670, target: { scene: 'MainScene', roomId: 3 } }],
      5: [{ x: 100, y: 640, target: { scene: 'MainScene', roomId: 2 } }, { x: 1500, y: 640, target: { scene: 'MainScene', roomId: 1 } }],
      6: [{ x: 100, y: 640, target: { scene: 'MainScene', roomId: 3 } }, { x: 1500, y: 640, target: { scene: 'MainScene', roomId: 2 } }]
    };
    if (!portalConfig[roomId]) return;

    this.portals = this.physics.add.staticGroup();
    portalConfig[roomId].forEach(portalInfo => {
      const p = this.add.circle(portalInfo.x, portalInfo.y, 30, 0xFF00FF, 0.3).setDepth(4);
      this.portals.add(p);
      p.setData('target', portalInfo.target);
    });
  }

  update() {
    if (!this.player) return;

    this.activePortal = null;
    this.physics.world.overlap(this.player, this.portals, (player, portal) => {
      this.activePortal = portal.getData('target');
    }, null, this);

    if (this.activeBubble) this.activeBubble.setPosition(this.player.x - 110, this.player.y - 150);

    if (this.nicknameText && this.nicknameBg) {
      this.nicknameText.setPosition(this.player.x, this.player.y + 78);
      this.nicknameBg.setPosition(this.player.x, this.player.y + 78);
    }

    this.otherPlayers.forEach(playerObj => {
      const { sprite, nicknameBg, nicknameText, chatBubble } = playerObj;
      nicknameBg.setPosition(sprite.x, sprite.y + 78);
      nicknameText.setPosition(sprite.x, sprite.y + 78);
      if (chatBubble) chatBubble.setPosition(sprite.x - 110, sprite.y - 150);
    });

    if (this.activePortal && Phaser.Input.Keyboard.JustDown(this.eKey)) {
      this.input.keyboard.enabled = false;
      if (this.bgm) {
        this.bgm.stop();
        this.bgm.destroy();
        this.bgm = null;
      }
      this.leaveRoom(this.roomId, this.nickname).finally(() => {
        window.userInfo.roomId = this.activePortal.roomId;
        this.scene.start(this.activePortal.scene, { userInfo: window.userInfo });
      });
      this.activePortal = null;
    }
  }

  handleKeyDown(event) {
    if (!this.input.keyboard.enabled) return;
    const keyMap = { ArrowUp: 'w', ArrowDown: 's', ArrowLeft: 'a', ArrowRight: 'd' };
    const dir = keyMap[event.key] || (['w', 'a', 's', 'd'].includes(event.key.toLowerCase()) ? event.key.toLowerCase() : null);
    if (dir && dir !== this.currentDirection) {
      this.currentDirection = dir;
      if (this.moveInterval) clearInterval(this.moveInterval);
      const payload = { nickname: this.nickname, direction: dir, roomId: this.roomId };
      stompClient.publish({ destination: "/app/move", body: JSON.stringify(payload) });
      this.moveInterval = setInterval(() => {
        stompClient.publish({ destination: "/app/move", body: JSON.stringify(payload) });
      }, 100);
    }
  }

  handleKeyUp(event) {
    if (!this.input.keyboard.enabled) return;
    const dirKeys = ['w', 'a', 's', 'd', 'ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'];
    if (dirKeys.includes(event.key)) {
      if (this.moveInterval) clearInterval(this.moveInterval);
      this.moveInterval = null;
      this.currentDirection = null;
      stompClient.publish({ destination: "/app/move", body: JSON.stringify({ nickname: this.nickname, direction: "stop", roomId: this.roomId }) });
    }
  }

  shutdown() {
    console.log(`BridgeScene shutdown: Cleaning up...`);
    this.input.keyboard.off('keydown', this.handleKeyDown, this);
    this.input.keyboard.off('keyup', this.handleKeyUp, this);
    if (this.customizationSub) this.customizationSub.unsubscribe();
    if (this.positionsSub) this.positionsSub.unsubscribe();
    if (this.chatSub) this.chatSub.unsubscribe();
    if (this.moveInterval) clearInterval(this.moveInterval);
    if (this.bgm) {
      this.bgm.stop();
      this.bgm.destroy();
      this.bgm = null;
    }
  }
}

export default BridgeScene;