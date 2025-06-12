class StartScene extends Phaser.Scene {
  constructor() {
    super('StartScene');
  }

  preload() {
    this.load.image('main_bg', 'assets/main.jpg');
    this.load.image('boy', 'assets/boy1.png');
    this.load.image('girl', 'assets/girl1.png');
    this.load.image('logo', 'assets/logo.png');
    this.load.image('start_button', 'assets/start_button.png'); // 시작하기 버튼
  }

  create() {
    // 배경 이미지
    this.add.image(800, 450, 'main_bg').setDisplaySize(1600, 900);

    // 로고
    this.add.image(960, 200, 'logo').setOrigin(0.5).setScale(0.4);

    // 캐릭터 그림자
    // this.add.rectangle(352, 840, 246, 49, 0x878787, 0.5)
    //   .setScale(1, -1).setAlpha(0.5).setDepth(1).setBlur?.(35);
    // this.add.rectangle(634, 843, 246, 49, 0x878787, 0.5)
    //   .setScale(1, -1).setAlpha(0.5).setDepth(1).setBlur?.(35);

    // 캐릭터 이미지
    this.add.image(350, 640, 'girl').setDisplaySize(325, 405);
    this.add.image(630, 642, 'boy').setDisplaySize(325, 405);

    // 설정 버튼
    const settingBtn = this.add.rectangle(110, 107, 94, 94, 0xB593CC)
      .setOrigin(0.5).setStrokeStyle(4, 0xffffff).setDepth(2);
    this.add.text(110, 107, '⚙', {
      fontSize: '36px',
      color: '#ffffff'
    }).setOrigin(0.5).setDepth(3);

    // 고객센터 버튼
    const helpBtn = this.add.rectangle(220, 107, 94, 94, 0xB593CC)
      .setOrigin(0.5).setStrokeStyle(4, 0xffffff).setDepth(2);
    this.add.text(220, 107, '?', {
      fontSize: '36px',
      color: '#ffffff'
    }).setOrigin(0.5).setDepth(3);

    // 시작 버튼 (버튼은 png 이미지 또는 Phaser 버튼으로 대체 가능)
    const startBtn = this.add.rectangle(1186.5, 669, 381, 94, 0xB593CC)
      .setOrigin(0.5).setStrokeStyle(2, 0xffffff).setDepth(3);
    this.add.text(1186.5, 669, '▶ 시작하기', {
      fontSize: '32px',
      fontFamily: 'Pretendard',
      fontStyle: 'bold',
      color: '#ffffff'
    }).setOrigin(0.5).setDepth(4);

    startBtn.setInteractive();
    startBtn.on('pointerdown', () => {
      this.scene.start('NicknameScene'); 
    });
  }
}

export default StartScene;
