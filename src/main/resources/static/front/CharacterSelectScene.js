const SERVER_URL = 'https://kuriverse.com'; 

class CharacterSelectScene extends Phaser.Scene {
  constructor() {
    super('CharacterSelectScene');
  }

  preload() {
    this.load.image('character_bg', 'assets/nicknamebg.png');
    this.load.image('boy1', 'assets/boy1.png');
    this.load.image('boy2', 'assets/boy2.png');
    this.load.image('boy3', 'assets/boy3.png');
    this.load.image('girl1', 'assets/girl1.png');
    this.load.image('girl2', 'assets/girl2.png');
    this.load.image('girl3', 'assets/girl3.png');
  }

  create(data) {
    const nickname = data?.nickname || window.userInfo?.nickname;
    if (!nickname) {
      alert('닉네임 인식 실패로 시작화면으로 돌아갑니다.');
      this.scene.start('NicknameScene');
      return;
    }

    const characterKeyToBodytype = {
      'boy1': 1, 'boy2': 2, 'boy3': 3,
      'girl1': 4, 'girl2': 5, 'girl3': 6
    };

    const saveCustomization = async (nickname, characterKey) => {
      const bodytypeInt = characterKeyToBodytype[characterKey];

      try {
        const response = await fetch(`${SERVER_URL}/api/customization/update`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify({ nickname: nickname, bodyType: bodytypeInt }) 
        });
        if (response.ok) {
          console.log("커스터마이징 저장 완료:", `nickname=${nickname}, bodytype=${bodytypeInt}`);
          this.scene.start('WorldMapScene', { nickname, character: characterKey });
        } else {
          const errorText = await response.text();
          alert(`커스터마이징 저장 실패: ${errorText}`);
        }
      } catch (error) {
        console.error("저장 중 오류:", error);
      }
    };

    this.add.image(800, 450, 'character_bg').setDisplaySize(1600, 900);
    this.add.rectangle(800, 100, 496, 72, 0xB593CC).setOrigin(0.5).setDepth(1);
    this.add.text(800, 100, '캐릭터를 선택해주세요', {
      fontFamily: 'Pretendard',
      fontSize: '32px',
      fontStyle: 'bold',
      color: '#ffffff'
    }).setOrigin(0.5).setDepth(2);

    const startX = 300;
    const gapX = 500;

    const characters = [
      { key: 'boy1', x: startX + 0 * gapX, y: 320 },
      { key: 'boy2', x: startX + 1 * gapX, y: 320 },
      { key: 'boy3', x: startX + 2 * gapX, y: 320 },
      { key: 'girl1', x: startX + 0 * gapX, y: 580 },
      { key: 'girl2', x: startX + 1 * gapX, y: 580 },
      { key: 'girl3', x: startX + 2 * gapX, y: 580 },
    ];

    characters.forEach(({ key, x, y }) => {
      const sprite = this.add.image(x, y, key)
        .setDisplaySize(200, 250)
        .setInteractive({ useHandCursor: true })
        .setDepth(3);
      sprite.on('pointerdown', () => {
        this.selectCharacter(key);
        saveCustomization(nickname, key);
      });
    });
  }

  selectCharacter(characterKey) {
    if (!window.userInfo) window.userInfo = {};
    window.userInfo.character = characterKey;
  }
}

export default CharacterSelectScene;