import { socket } from './game.js'; 
import { stompClient } from './game.js';

class WorldMapScene extends Phaser.Scene {
  constructor() {
    super('WorldMapScene');
  }

  preload() {
    this.load.image('world_bg', 'assets/worldbg.png'); 
  }

  create() {
    this.add.image(800, 450, 'world_bg').setDisplaySize(1600, 900);

    const createRoomButton = (text, x, y, roomId) => {
      const btn = this.add.rectangle(x, y, 287, 72, 0xF8F2FC)
        .setStrokeStyle(2, 0xB593CC)
        .setInteractive()
        .setDepth(2);

      this.add.text(x, y, text, {
        font: '28px Pretendard',
        color: '#B593CC'
      }).setOrigin(0.5).setDepth(3);

      btn.on('pointerdown', () => {
        window.userInfo.roomId = roomId;
        window.userInfo.path = text; 
        this.scene.start('MainScene');
      });
    };

    
    createRoomButton('교실', 1250, 410, 1);
    createRoomButton('문화공간', 550, 280, 3);
    createRoomButton('공원', 500, 700, 2); 
  }
}

export default WorldMapScene;