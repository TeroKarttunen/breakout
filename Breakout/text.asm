score_1 = $02ae
score_2 = $02af
score_3 = $02b0
score_4 = $02b1
add_to_score = $02b4
score_msd_temp = $02b5
score_lsd_temp = $02b6


reset_score:
  lda #$00
  sta score_1
  sta score_2
  sta score_3
  sta score_4
  sta score_lsd
  sta score_msd
  sta game_time_1
  sta game_time_2
  sta game_time_3
  rts

score_text:
;    "1234567890123456789012345678901234567890"
!scr                             "score: 0000 "

show_score_text:
  ldx #11
- lda score_text,x
  sta $0400 + (0*40) + 28,x
  lda #$00
  sta color_mem + (0*40) + 28,x
  dex
  bpl -
  clc
  lda score_1
  adc #$30
  sta $0400 + (0*40) + 35
  clc
  lda score_2
  adc #$30
  sta $0400 + (0*40) + 36
  clc
  lda score_3
  adc #$30
  sta $0400 + (0*40) + 37
  clc
  lda score_4
  adc #$30
  sta $0400 + (0*40) + 38
  rts

update_score:
  clc
  lda score_1
  adc #$30
  sta $0400 + (0*40) + 35
  clc
  lda score_2
  adc #$30
  sta $0400 + (0*40) + 36
  clc
  lda score_3
  adc #$30
  sta $0400 + (0*40) + 37
  clc
  lda score_4
  adc #$30
  sta $0400 + (0*40) + 38
  rts
  
;print_pos: debugging routine
  lda #48
  sta screen_mem
  lda ball_x_overflow 
  beq +
  lda #49
  sta screen_mem + 0
+ lda ball_x
  and #$f0
  lsr
  lsr
  lsr
  lsr
  clc
  adc #48
  sta screen_mem +1
  lda ball_x
  and #$0f
  clc
  adc #48
  sta screen_mem +2
  lda ball_y
  and #$f0
  lsr
  lsr
  lsr
  lsr
  clc
  adc #48
  sta screen_mem +4
  lda ball_y
  and #$0f
  clc
  adc #48
  sta screen_mem +5
  rts
  
increase_score:
  clc
  lda score_lsd
  adc add_to_score
  sta score_lsd
  lda score_msd
  adc #$00
  sta score_msd
  rts

; 1000 = 3E8
; 2000 = 7D0
; 3000 = BB8
; 4000 = FA0
; 5000 = 1388
; 6000 = 1770
; 7000 = 1B58
; 8000 = 1F40
; 9000 = 2328
; 10000 = 2710

calculate_score:
  lda #$00
  sta score_1
  sta score_2
  sta score_3
  sta score_4
  lda score_msd 
  sta score_msd_temp
  lda score_lsd
  sta score_lsd_temp
thousands:
  lda score_msd_temp
  cmp #$03
  bcc hundreds
  bne dec_thousand
  lda score_lsd_temp
  cmp #$E8
  bcc hundreds
dec_thousand:
  lda score_lsd_temp
  sec
  sbc #$E8
  sta score_lsd_temp
  lda score_msd_temp
  sbc #$03
  sta score_msd_temp
  inc score_1
  jmp thousands
hundreds:
  lda score_msd_temp
  bne dec_hundred
  lda score_lsd_temp
  cmp #100
  bcc tens
dec_hundred:
  lda score_lsd_temp
  sec
  sbc #100
  sta score_lsd_temp
  lda score_msd_temp
  sbc #$00
  sta score_msd_temp
  inc score_2
  jmp hundreds
tens:
  lda score_lsd_temp
  cmp #10
  bmi ones
  sbc #10
  sta score_lsd_temp
  inc score_3
  jmp tens
ones:
  lda score_lsd_temp
  sta score_4
  rts
  
print_speed:
  ldx #0
  lda speed
  cmp #0
  bne +
- lda speed_0_text, x
  sta $0400 + (0*40) + 0, x
  inx
  cpx #6
  bne -
  rts
+ cmp #1
  bne +
- lda speed_1_text, x
  sta $0400 + (0*40) + 0, x
  inx
  cpx #6
  bne -
  rts
+ cmp #2
  bne +
- lda speed_2_text, x
  sta $0400 + (0*40) + 0, x
  inx
  cpx #6
  bne -
  rts
+ cmp #3
  bne +
- lda speed_3_text, x
  sta $0400 + (0*40) + 0, x
  inx
  cpx #6
  bne -
+ rts  

show_level_text:
  ldx #0
  lda current_level
  cmp #1
  bne +
- lda level_one_text, x
  sta $0400 + (0*40) + 14, x
  inx
  cpx #11
  bne -
  rts
+ cmp #2
  bne +
- lda level_two_text, x
  sta $0400 + (0*40) + 14, x
  inx
  cpx #11
  bne -
  rts
+ cmp #3
  bne +
- lda level_three_text, x
  sta $0400 + (0*40) + 14, x
  inx
  cpx #11
  bne -
  rts  
+ cmp #4
  bne +
- lda level_four_text, x
  sta $0400 + (0*40) + 14, x
  inx
  cpx #11
  bne -
  rts
+ rts

show_game_over:
  ldx #0
- lda game_over_text, x
  sta $0400 + (12*40) + 14, x
  lda #$00
  sta color_mem + (12*40) + 14,x
  inx
  cpx #9
  bne -
  rts

level_one_text:
;     12345678901
!scr "level one  "

level_two_text:
!scr "level two  "

level_three_text:
!scr "level three"

level_four_text:
!scr "level four "

game_over_text:
!scr "game over"

speed_0_text:
!scr "slow  "

speed_1_text:
!scr "medium"

speed_2_text:
!scr "fast  "

speed_3_text:
!scr "expert"

joystick_text:
!scr "joystk"

paddle_text:
!scr "paddle"
