temp_variable = $02aa
tiles_hit_counter = $02ad
current_level = $02c4
tiles_left = $02cc ; updated by draw_tiles

; this could be improved
draw_tiles:
  ldx #0
  stx tiles_left
draw_loop:
  lda tiles,x
  beq +
  inc tiles_left 
  lda #76 + 128; left tile char
  sta $0400 + (2*40),x 
  lda #111 + 128; right tile char
  sta $0400 + (2*40) + 1,x
  lda tile_colors, x
  sta color_mem + (2*40),x
  sta color_mem + (2*40) + 1,x
  inx
  inx
  cpx #240
  bne draw_loop
  rts 
+ lda #$20
  sta $0400 + (2*40),x ; clear first
  sta $0400 + (2*40) +1 ,x ; clear second
  inx
  inx
  cpx #240
  bne draw_loop
  rts
  
; input: A = level, starting from 1
reset_level:
  ldx #0
  sta current_level
  cmp #1
  bne +
- lda level_one, x
  sta tiles, x
  inx
  cpx #240
  bne -
  rts
+ cmp #2
  bne +
- lda level_two, x
  sta tiles, x
  inx
  cpx #240
  bne -
  rts
+ cmp #3
  bne +
- lda level_three, x
  sta tiles, x
  inx
  cpx #240
  bne -
  rts  
+ cmp #4
  bne +
- lda level_four, x
  sta tiles, x
  inx
  cpx #240
  bne -
  rts
+ rts

check_next_level:
  lda tiles_left
  bne check_next_level_return
  ; no tiles
  ldx current_level
  cpx #4
  bne +
  jsr inc_speed ; increase speed for next round
  ldx #0
+ inx
  txa
  sta current_level
  jsr reset_level
  jsr show_score_text
  jsr show_level_text
  jsr draw_tiles
  jsr print_speed
check_next_level_return:
  rts
  
handle_collision:
  clc
  lda ball_y 
  cmp #$70 ; 70 <= X
  bcc +
  rts  ; no tiles below this line
+ cmp #$68 ; 68 <= Y <= 6F single(200)
  bcc +
  ldx #200
  jmp handle_single_collision
+ cmp #$65 ; 65 <= Y <= 67 double(160)
  bcc +
  ldx #160
  jmp handle_double_collision
+ cmp #$60 ; 60 <= Y <= 64 single(160)
  bcc +
  ldx #160
  jmp handle_single_collision
+ cmp #$5D ; 5D <= Y <= 5F double(120)
  bcc +
  ldx #120
  jmp handle_double_collision
+ cmp #$58 ; 58 <= Y <= 5C single(120)
  bcc +  
  ldx #120
  jmp handle_single_collision
+ cmp #$55 ; 55 <= Y <= 57 double(80)
  bcc +
  ldx #80
  jmp handle_double_collision
+ cmp #$50 ; 50 <= Y <= 54 single(80)
  bcc +
  ldx #80
  jmp handle_single_collision
+ cmp #$4D ; 4D <= Y <= 4F double(40)
  bcc + 
  ldx #40
  jmp handle_double_collision
+ cmp #$48 ; 48 <= Y <= 4C single(40)
  bcc +
  ldx #40
  jmp handle_single_collision
+ cmp #$45 ; 45 <= Y <= 47 double(0)
  bcc +
  ldx #0
  jmp handle_double_collision
+ cmp #$3D ; 3D <= Y <= 44 single(0)
  bcc +
  ldx #0
  jmp handle_single_collision
+ rts

handle_single_collision: ; ie single row collision = need to check one row only
  lda ball_x_overflow
  beq +
  jmp overflow
+ lda ball_x
  cmp #$F8 ; F8 <= X <= FF
  bcc +
  ldy #28 
  jmp handle_single_collision2
+ cmp #$F5 ; F5 <= X <= F7 double
  bcc + 
  ldy #26
  jmp handle_single_collision3
+ cmp #$E8 ; E8 <= X <= F4 single
  bcc +
  ldy #26
  jmp handle_single_collision2
+ cmp #$E5 ; E5 <= X <= E7 double
  bcc + 
  ldy #24
  jmp handle_single_collision3
+ cmp #$D8 ; D8 <= X <= E4 single
  bcc +
  ldy #24
  jmp handle_single_collision2
+ cmp #$D5 ; D5 <= X <= D7 double
  bcc + 
  ldy #22
  jmp handle_single_collision3
+ cmp #$C8 ; C8 <= X <= D4 single
  bcc +
  ldy #22
  jmp handle_single_collision2
+ cmp #$C5 ; C5 <= X <= C7 double
  bcc + 
  ldy #20
  jmp handle_single_collision3
+ cmp #$B8 ; B8 <= X <= C4 single
  bcc +
  ldy #20
  jmp handle_single_collision2
+ cmp #$B5 ; B5 <= X <= B7 double
  bcc + 
  ldy #18
  jmp handle_single_collision3
+ cmp #$A8 ; A8 <= X <= B4 single
  bcc +
  ldy #18
  jmp handle_single_collision2
+ cmp #$A5 ; A5 <= X <= A7 double
  bcc + 
  ldy #16
  jmp handle_single_collision3
+ cmp #$98 ; 98 <= X <= A4 single
  bcc +
  ldy #16
  jmp handle_single_collision2
+ cmp #$95 ; 95 <= X <= 97 double
  bcc + 
  ldy #14
  jmp handle_single_collision3
+ cmp #$88 ; 88 <= X <= 94 single
  bcc +
  ldy #14
  jmp handle_single_collision2
+ cmp #$85 ; 85 <= X <= 87 double
  bcc + 
  ldy #12
  jmp handle_single_collision3
+ cmp #$78 ; 78 <= X <= 84 single
  bcc +
  ldy #12
  jmp handle_single_collision2
+ cmp #$75 ; 75 <= X <= 77 double
  bcc + 
  ldy #10
  jmp handle_single_collision3
+ cmp #$68 ; 68 <= X <= 74 single
  bcc +
  ldy #10
  jmp handle_single_collision2
+ cmp #$65 ; 65 <= X <= 67 double
  bcc + 
  ldy #8
  jmp handle_single_collision3
+ cmp #$58 ; 58 <= X <= 64 single
  bcc +
  ldy #8
  jmp handle_single_collision2
+ cmp #$55 ; 55 <= X <= 57 double
  bcc + 
  ldy #6
  jmp handle_single_collision3
+ cmp #$48 ; 48 <= X <= 54 single
  bcc +
  ldy #6
  jmp handle_single_collision2
+ cmp #$45 ; 45 <= X <= 47 double
  bcc + 
  ldy #4
  jmp handle_single_collision3
+ cmp #$38 ; 38 <= X <= 44 single
  bcc +
  ldy #4
  jmp handle_single_collision2
+ cmp #$35 ; 35 <= X <= 37 double
  bcc + 
  ldy #2
  jmp handle_single_collision3
+ cmp #$28 ; 28 <= X <= 34 single
  bcc +
  ldy #2
  jmp handle_single_collision2
+ cmp #$25 ; 25 <= X <= 27 double
  bcc + 
  ldy #0
  jmp handle_single_collision3
+ cmp #$18 ; 18 <= X <= 24 single
  bcc +
  ldy #0
  jmp handle_single_collision2
+ rts  
overflow:
  lda ball_x
  cmp #05 ; 100 <= X <= 104 single 
  bpl +
  ldy #28 
  jmp handle_single_collision2
+ cmp #$08 ; 105 <= X <= 107 double
  bpl +
  ldy #28 
  jmp handle_single_collision3
+ cmp #$15 ; 108 <= X <= 114 single
  bpl +
  ldy #30
  jmp handle_single_collision2
+ cmp #$18 ; 115 <= X <= 117 double
  bpl +
  ldy #30
  jmp handle_single_collision3
+ cmp #$25 ; 118 <= X <= 124 single
  bpl +
  ldy #32
  jmp handle_single_collision2
+ cmp #$28 ; 125 <= X <= 127 double
  bpl +
  ldy #32
  jmp handle_single_collision3
+ cmp #$35 ; 128 <= X <= 134 single
  bpl +
  ldy #34
  jmp handle_single_collision2
+ cmp #$38 ; 135 <= X <= 137 double
  bpl +
  ldy #34
  jmp handle_single_collision3
+ cmp #$45 ; 138 <= X <= 144 single
  bpl +
  ldy #36
  jmp handle_single_collision2
+ cmp #$48 ; 145 <= X <= 147 double
  bpl +
  ldy #36
  jmp handle_single_collision3
+ cmp #$53 ; 148 <= X <= 152 single
  bpl +
  ldy #38
  jmp handle_single_collision2
+ rts

handle_single_collision2: ; only one tile can be hit
  sty temp_variable
  clc
  txa 
  adc temp_variable
  stx temp_variable
  tax
  lda tiles,x
  beq +
  lda #$00
  sta tiles,x
  sta tiles+1,x
  inc tiles_hit_counter
+ ldx temp_variable
  rts

handle_single_collision3: ; up to two tiles can be hit
  sty temp_variable
  clc
  txa 
  adc temp_variable
  stx temp_variable
  tax
  lda tiles,x
  beq +
  lda #$00
  sta tiles,x
  sta tiles+1,x
  inc tiles_hit_counter
+ lda tiles+2,x 
  beq +
  lda #$00
  sta tiles+2,x
  sta tiles+3,x
  inc tiles_hit_counter
+ ldx temp_variable
  rts

handle_double_collision: ; double row collision = need to check two rows
  jsr handle_single_collision
  clc
  lda temp_variable
  adc #40
  tax
  jsr handle_single_collision
  rts
    
tile_colors:
!byte $00, $00, $02, $02, $03, $03, $04, $04, $05, $05, $06, $06, $07, $07, $08, $08, $09, $09, $0a, $0a, $0b, $0b, $0c, $0c, $0d, $0d, $0e, $0e, $0f, $0f
!byte $00, $00, $02, $02, $03, $03, $04, $04, $05, $05, $06, $06, $07, $07, $08, $08, $09, $09, $0a, $0a, $0b, $0b, $0c, $0c, $0d, $0d, $0e, $0e, $0f, $0f
!byte $00, $00, $02, $02, $03, $03, $04, $04, $05, $05, $06, $06, $07, $07, $08, $08, $09, $09, $0a, $0a, $0b, $0b, $0c, $0c, $0d, $0d, $0e, $0e, $0f, $0f
!byte $00, $00, $02, $02, $03, $03, $04, $04, $05, $05, $06, $06, $07, $07, $08, $08, $09, $09, $0a, $0a, $0b, $0b, $0c, $0c, $0d, $0d, $0e, $0e, $0f, $0f
!byte $00, $00, $02, $02, $03, $03, $04, $04, $05, $05, $06, $06, $07, $07, $08, $08, $09, $09, $0a, $0a, $0b, $0b, $0c, $0c, $0d, $0d, $0e, $0e, $0f, $0f
!byte $00, $00, $02, $02, $03, $03, $04, $04, $05, $05, $06, $06, $07, $07, $08, $08, $09, $09, $0a, $0a, $0b, $0b, $0c, $0c, $0d, $0d, $0e, $0e, $0f, $0f
!byte $00, $00, $02, $02, $03, $03, $04, $04, $05, $05, $06, $06, $07, $07, $08, $08, $09, $09, $0a, $0a, $0b, $0b, $0c, $0c, $0d, $0d, $0e, $0e, $0f, $0f
!byte $00, $00, $02, $02, $03, $03, $04, $04, $05, $05, $06, $06, $07, $07, $08, $08, $09, $09, $0a, $0a, $0b, $0b, $0c, $0c, $0d, $0d, $0e, $0e, $0f, $0f

; level one
level_one:
!byte $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00
!byte $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01
!byte $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01
!byte $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01
!byte $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01
!byte $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01

; level two
level_two:
!byte $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00
!byte $00, $00, $00, $00, $00, $00, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $00, $00, $00, $00, $00, $00
!byte $00, $00, $00, $00, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $00, $00, $00, $00
!byte $00, $00, $00, $00, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $00, $00, $00, $00
!byte $00, $00, $00, $00, $00, $00, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $00, $00, $00, $00, $00, $00
!byte $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00

; level three
level_three:
!byte $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00
!byte $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00
!byte $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00
!byte $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00
!byte $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00
!byte $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00, $01, $01, $00, $00

; level four
level_four:
!byte $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $01, $01, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $01, $01, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00
!byte $00, $00, $01, $01, $00, $00, $00, $00, $01, $01, $01, $01, $01, $01, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $01, $01, $01, $01, $01, $01, $00, $00, $00, $00, $01, $01, $00, $00
!byte $01, $01, $01, $01, $01, $01, $00, $00, $01, $01, $01, $01, $01, $01, $00, $00, $00, $00, $01, $01, $01, $01, $00, $00, $00, $00, $01, $01, $01, $01, $01, $01, $00, $00, $01, $01, $01, $01, $01, $01
!byte $01, $01, $01, $01, $01, $01, $00, $00, $00, $00, $01, $01, $00, $00, $00, $00, $01, $01, $01, $01, $01, $01, $01, $01, $00, $00, $00, $00, $01, $01, $00, $00, $00, $00, $01, $01, $01, $01, $01, $01
!byte $00, $00, $01, $01, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $01, $01, $01, $01, $01, $01, $01, $01, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $01, $01, $00, $00
!byte $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $01, $01, $01, $01, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00


; all tiles
tiles:
!byte $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01
!byte $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01
!byte $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01
!byte $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01
!byte $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01
!byte $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01, $01


