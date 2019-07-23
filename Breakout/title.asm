* = $5000

draw_title:
  ; clear screen with space character
  ;lda #$20
  ;ldx #$00
  ;sta screen_mem,x
  ;sta screen_mem + 256,x
  ;sta screen_mem + 512,x
  ;sta screen_mem + 768,x
  ;dex
  ;bne -
  lda #$00
  ldx #$00
- lda title_colors,x
  sta color_mem,x
  lda title_colors + 256,x
  sta color_mem + 256,x
  lda title_colors + 512,x
  sta color_mem + 512,x
  lda title_colors + 768,x
  sta color_mem + 768,x
  dex
  bne -
- lda title_screen,x
  sta screen_mem,x
  lda title_screen + 256,x
  sta screen_mem + 256,x
  lda title_screen + 512,x
  sta screen_mem + 512,x
  lda title_screen + 768,x
  sta screen_mem + 768,x
  dex
  bne -
rts

print_title_text:
; speed text
  ldx #0
  lda speed
  cmp #0
  bne +
- lda speed_0_text, x
  sta $0400 + (16*40) + 29, x
  inx
  cpx #6
  bne -
  jmp print_paddle_text
+ cmp #1
  bne +
- lda speed_1_text, x
  sta $0400 + (16*40) + 29, x
  inx
  cpx #6
  bne -
  jmp print_paddle_text
+ cmp #2
  bne +
- lda speed_2_text, x
  sta $0400 + (16*40) + 29, x
  inx
  cpx #6
  bne -
  jmp print_paddle_text
+ cmp #3
  bne print_paddle_text
- lda speed_3_text, x
  sta $0400 + (16*40) + 29, x
  inx
  cpx #6
  bne -
print_paddle_text:
; input_method
  ldx #0
  lda input_method
  cmp #0
  bne +
- lda paddle_text, x
  sta $0400 + (20*40) + 29, x
  inx
  cpx #6
  bne -
  rts
+ cmp #1
  bne +
- lda joystick_text, x
  sta $0400 + (20*40) + 29, x
  inx
  cpx #6
  bne -
  rts
+ rts


  

title_screen:
;screen char data
!byte  $D5,$C0,$C0,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C9
!byte  $C2,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$C2
!byte  $C2,$20,$20,$20,$D5,$C0,$C3,$C9,$D5,$C3,$C3,$C9,$D5,$C3,$C3,$C9,$D5,$C0,$C3,$C9,$D5,$C9,$D5,$C9,$D5,$C0,$C0,$C9,$D5,$C9,$D5,$C9,$C0,$C0,$C0,$C0,$20,$20,$20,$C2
!byte  $C2,$20,$20,$20,$C2,$D5,$C9,$C2,$C2,$D5,$C9,$C2,$C2,$D5,$C3,$Cb,$C2,$D5,$C9,$C2,$C2,$C2,$C2,$C2,$C2,$D5,$C9,$C2,$C2,$C2,$C2,$C2,$C0,$C9,$D5,$C0,$20,$20,$20,$C2
!byte  $C2,$20,$20,$20,$C2,$Ca,$Cb,$Cb,$C2,$Ca,$Cb,$Cb,$C2,$Ca,$C3,$C9,$C2,$Ca,$Cb,$C2,$C2,$Ca,$Cb,$Cb,$C2,$C2,$C2,$C2,$C2,$C2,$C2,$C2,$A0,$C2,$C2,$A0,$20,$20,$20,$C2
!byte  $C2,$20,$20,$20,$C2,$D5,$C9,$C9,$C2,$D5,$C9,$C9,$C2,$D5,$C3,$Cb,$C2,$D5,$C9,$C2,$C2,$D5,$C9,$C9,$C2,$C2,$C2,$C2,$C2,$C2,$C2,$C2,$A0,$C2,$C2,$A0,$20,$20,$20,$C2
!byte  $C2,$20,$20,$20,$C2,$Ca,$Cb,$C2,$C2,$C2,$C2,$C2,$C2,$Ca,$C3,$C9,$C2,$C2,$C2,$C2,$C2,$C2,$C2,$C2,$C2,$Ca,$Cb,$C2,$C2,$Ca,$Cb,$C2,$A0,$C2,$C2,$A0,$20,$20,$20,$C2
!byte  $C2,$20,$20,$20,$Ca,$C3,$C3,$Cb,$Ca,$Cb,$Ca,$Cb,$Ca,$C3,$C3,$Cb,$Ca,$Cb,$Ca,$Cb,$Ca,$Cb,$Ca,$Cb,$Ca,$C0,$C0,$Cb,$Ca,$C0,$C0,$Cb,$A0,$Ca,$Cb,$A0,$20,$20,$20,$C2
!byte  $C2,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$C2
!byte  $Ca,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$C3,$Cb
!byte  $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$02,$19,$20,$0b,$01,$12,$13,$0b,$09,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$06,$31,$20,$43,$20,$13,$14,$01,$12,$14,$2f,$0e,$05,$17,$20,$07,$01,$0d,$05,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$06,$33,$20,$43,$20,$09,$0e,$03,$12,$05,$01,$13,$05,$20,$13,$10,$05,$05,$04,$20,$20,$20,$20,$28,$20,$20,$20,$20,$20,$20,$29,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$06,$35,$20,$43,$20,$04,$05,$03,$12,$05,$01,$13,$05,$20,$13,$10,$05,$05,$04,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$06,$37,$20,$40,$20,$14,$0f,$07,$07,$0c,$05,$20,$0a,$0f,$19,$2f,$10,$01,$04,$04,$0c,$05,$20,$28,$20,$20,$20,$20,$20,$20,$29,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20
!byte  $20,$00,$03,$0f,$10,$19,$0c,$05,$06,$14,$20,$32,$30,$31,$38,$20,$20,$20,$20,$20,$01,$0c,$0c,$20,$17,$12,$0f,$0e,$07,$13,$20,$12,$05,$13,$05,$12,$16,$05,$04,$20

title_colors:
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$02,$02,$02,$02,$03,$03,$03,$03,$04,$04,$04,$04,$05,$05,$05,$05,$06,$06,$06,$06,$07,$07,$07,$07,$08,$08,$08,$08,$09,$09,$09,$09,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$02,$02,$02,$02,$03,$03,$03,$03,$04,$04,$04,$04,$05,$05,$05,$05,$06,$06,$06,$06,$07,$07,$07,$07,$08,$08,$08,$08,$09,$09,$09,$09,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$02,$02,$02,$02,$03,$03,$03,$03,$04,$04,$04,$04,$05,$05,$05,$05,$06,$06,$06,$06,$07,$07,$07,$07,$08,$08,$08,$08,$09,$09,$09,$09,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$02,$02,$02,$02,$03,$03,$03,$03,$04,$04,$04,$04,$05,$05,$05,$05,$06,$06,$06,$06,$07,$07,$07,$07,$08,$08,$08,$08,$09,$09,$09,$09,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$02,$02,$02,$02,$03,$03,$03,$03,$04,$04,$04,$04,$05,$05,$05,$05,$06,$06,$06,$06,$07,$07,$07,$07,$08,$08,$08,$08,$09,$09,$09,$09,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$02,$02,$02,$02,$03,$03,$03,$03,$04,$04,$04,$04,$05,$05,$05,$05,$06,$06,$06,$06,$07,$07,$07,$07,$08,$08,$08,$08,$09,$09,$09,$09,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
!byte  $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
